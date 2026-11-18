package com.chinaex123.throwing_disc.entity;

import com.chinaex123.throwing_disc.api.DiscTier;
import com.chinaex123.throwing_disc.enchantment.TDEnchantmentHelper;
import com.chinaex123.throwing_disc.item.DiscItem;
import com.chinaex123.throwing_disc.init.TDEntities;
import com.chinaex123.throwing_disc.init.TDDiscTiers;
import com.chinaex123.throwing_disc.init.TDSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 飞盘弹射物。
 * <p>
 * 掷出后沿直线飞行并按距离返回投掷者：飞行中可命中生物造成伤害、
 * 在方块表面反弹，配合冲撞附魔可破坏方块并携带掉落物。
 * 附魔影响射程（空气动力学）、反弹次数（回旋）、返回速度（磁返）、
 * 伤害（贯穿）与耐久保留（保留）。返回时追向玩家并被接住，
 * 超时或玩家失效时掉落为物品。
 */
public class DiscProjectile extends Projectile {

    /** 同步数据：飞盘物品。 */
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(DiscProjectile.class, EntityDataSerializers.ITEM_STACK);

    /** 同步数据：是否正在返回。 */
    private static final EntityDataAccessor<Boolean> DATA_RETURNING = SynchedEntityData.defineId(DiscProjectile.class, EntityDataSerializers.BOOLEAN);

    /** 每格飞行所需的刻数。 */
    public static final double FLIGHT_TICKS_PER_BLOCK = 1.5D;

    /** 基础射程（格）。 */
    public static final double BASE_RANGE = 16.0D;

    /** 投掷速度。 */
    public static final float THROW_SPEED = (float) (1.0D / FLIGHT_TICKS_PER_BLOCK);

    /** 基础飞行刻数。 */
    public static final int BASE_FLIGHT_TICKS = ticksForDistance(BASE_RANGE);

    /** 空气动力学每级增加的射程（格）。 */
    public static final double AERODYNAMICS_RANGE_PER_LEVEL = 1.0D;

    /** 每次反弹增加的射程（格）。 */
    public static final double BOUNCE_RANGE_BONUS = 3.0D;

    /** 每次反弹增加的飞行刻数。 */
    public static final int BOUNCE_TICK_BONUS = ticksForDistance(BOUNCE_RANGE_BONUS);

    /** 基础最大反弹次数。 */
    public static final int BASE_MAX_BOUNCES = 5;

    /** 磁返每级提高的返回速度。 */
    public static final double MAGNETIC_RETURN_SPEED_PER_LEVEL = 0.2D;

    /** 最大射程（格）。 */
    public static final double MAX_RANGE = 64.0D;

    /** 最大飞行刻数。 */
    public static final int MAX_FLIGHT_TICKS = ticksForDistance(MAX_RANGE);

    /** 返回阶段的最大持续刻数。 */
    private static final int MAX_RETURN_TICKS = 100;

    /** 接住飞盘的距离阈值。 */
    private static final double CATCH_DISTANCE = 1.4D;

    /**
     * 将距离换算为飞行刻数。
     *
     * @param distance 距离（格）
     * @return 飞行刻数，至少为 1
     */
    private static int ticksForDistance(double distance) {
        return Math.max(1, (int) Math.ceil(distance * FLIGHT_TICKS_PER_BLOCK));
    }

    /**
     * 计算当前基础射程，含空气动力学加成。
     *
     * @return 基础射程（格）
     */
    private double baseRange() {
        int level = TDEnchantmentHelper.getAerodynamicsLevel(this.getItem(), this.level());
        return BASE_RANGE + level * AERODYNAMICS_RANGE_PER_LEVEL;
    }

    /**
     * 计算当前基础飞行刻数。
     *
     * @return 基础飞行刻数
     */
    private int baseFlightTicks() {
        return ticksForDistance(this.baseRange());
    }

    /**
     * 计算当前最大反弹次数，含回旋加成。
     *
     * @return 最大反弹次数
     */
    private int maxBounces() {
        int level = TDEnchantmentHelper.getRicochetLevel(this.getItem(), this.level());
        return BASE_MAX_BOUNCES + level;
    }

    /**
     * 计算当前返回速度倍率，含磁返加成。
     *
     * @return 返回速度倍率
     */
    private double returnSpeedMultiplier() {
        int level = TDEnchantmentHelper.getMagneticReturnLevel(this.getItem(), this.level());
        return 1.0D + level * MAGNETIC_RETURN_SPEED_PER_LEVEL;
    }

    /** 是否已进入返回阶段。 */
    private boolean returning;

    /** 已飞行刻数。 */
    private int flightTicks;

    /** 返回阶段已持续刻数。 */
    private int returnTicks;

    /** 到达该刻数时开始返回。 */
    private int returnAtTick = BASE_FLIGHT_TICKS;

    /** 到达该距离时开始返回。 */
    private double returnAtDistance = BASE_RANGE;

    /** 已飞行距离。 */
    private double traveled;

    /** 已反弹次数。 */
    private int bounces;

    /** 投掷时使用的手。 */
    private InteractionHand fromHand = InteractionHand.MAIN_HAND;

    /** 本次飞行中已命中的实体 UUID。 */
    private final Set<UUID> hitEntities = new HashSet<>();

    /** 冲撞破坏并携带的掉落物。 */
    private final List<ItemStack> carriedDrops = new ArrayList<>();

    /**
     * 构造飞盘弹射物。
     *
     * @param type  实体类型
     * @param level 世界
     */
    public DiscProjectile(EntityType<? extends DiscProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /**
     * 构造由玩家掷出的飞盘弹射物。
     * <p>
     * 记录投掷者、飞盘物品与投掷手，并按附魔计算返回距离与刻数。
     *
     * @param level 世界
     * @param owner 投掷者
     * @param stack 飞盘物品
     * @param hand  投掷手
     */
    public DiscProjectile(Level level, Player owner, ItemStack stack, InteractionHand hand) {
        this(TDEntities.DISC_ENTITY.get(), level);
        this.setOwner(owner);
        this.setItem(stack);
        this.fromHand = hand;
        this.returnAtDistance = this.baseRange();
        this.returnAtTick = this.baseFlightTicks();
    }

    /**
     * 注册同步数据。
     *
     * @param builder 同步数据构建器
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_RETURNING, false);
    }

    /**
     * 获取飞盘物品。
     *
     * @return 飞盘物品堆叠
     */
    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    /**
     * 设置飞盘物品。
     *
     * @param stack 飞盘物品堆叠
     */
    public void setItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack);
    }

    /**
     * 判断是否正在返回。
     *
     * @return 正在返回返回 true
     */
    public boolean isReturning() {
        return this.entityData.get(DATA_RETURNING);
    }

    /**
     * 获取飞盘等级。
     * <p>
     * 物品不是飞盘时回退到铜级。
     *
     * @return 飞盘等级
     */
    public DiscTier getTier() {
        return this.getItem().getItem() instanceof DiscItem disc ? disc.getTier() : TDDiscTiers.COPPER;
    }

    /**
     * 判断是否免疫火焰。
     *
     * @return 当前等级防火时返回 true
     */
    @Override
    public boolean fireImmune() {
        return this.getTier().fireResistant();
    }

    /**
     * 每刻更新。
     * <p>
     * 客户端不处理逻辑；物品为空或投掷者失效时掉落并移除；
     * 否则按返回或飞行阶段分别推进。
     */
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.getItem().isEmpty()) {
            this.dropCarriedDrops();
            this.discard();
            return;
        }

        if (this.isReturning()) {
            this.tickReturning();
        } else {
            Entity owner = this.getOwner();
            if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
                this.dropAndDiscard();
                return;
            }
            this.tickFlying();
        }
    }

    /**
     * 推进飞行阶段。
     * <p>
     * 沿速度方向做方块裁剪并检测沿途实体命中；
     * 命中方块时优先尝试冲撞破坏，其次按剩余反弹次数反弹或开始返回；
     * 飞行超过刻数或距离限制时开始返回。
     */
    private void tickFlying() {
        Vec3 start = this.position();
        Vec3 movement = this.getDeltaMovement();

        if (movement.lengthSqr() < 1.0E-7D) {
            this.startReturning();
            return;
        }

        Vec3 end = start.add(movement);
        BlockHitResult blockHit = this.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
        ));
        Vec3 traceEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;

        this.hitEntitiesAlong(start, traceEnd);

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            if (this.tryBreakBlock(blockHit)) {
                // 冲撞附魔破坏方块后继续沿原方向飞行，不反弹。
                this.setPos(end.x, end.y, end.z);
            } else if (this.bounces >= this.maxBounces()) {
                // 回旋提供的反弹次数已用完，贴住命中面后直接返回。
                Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
                this.setPos(
                        blockHit.getLocation().x + normal.x * 0.08D,
                        blockHit.getLocation().y + normal.y * 0.08D,
                        blockHit.getLocation().z + normal.z * 0.08D
                );
                this.startReturning();
                return;
            } else {
                this.bounce(blockHit);
            }
        } else {
            this.setPos(end.x, end.y, end.z);
        }

        if (this.isRemoved()) {
            return;
        }

        this.flightTicks++;
        this.traveled += movement.length();
        ProjectileUtil.rotateTowardsMovement(this, 1.0F);
        this.spawnFireTrail();

        if (this.flightTicks >= this.returnAtTick || this.traveled >= this.returnAtDistance) {
            this.startReturning();
        }
    }

    /**
     * 检测飞行路径上命中的实体。
     * <p>
     * 在扩大后的搜索范围内收集候选实体，按距离排序后
     * 逐一与路径做裁剪检测，命中则处理该实体。
     *
     * @param start 起点
     * @param end   终点
     */
    private void hitEntitiesAlong(Vec3 start, Vec3 end) {
        AABB searchBox = this.getBoundingBox()
                .expandTowards(end.subtract(start))
                .inflate(0.65D);
        List<Entity> candidates = new ArrayList<>(this.level().getEntities(this, searchBox, this::canHitEntity));
        candidates.sort(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(start)));

        for (Entity target : candidates) {
            AABB targetBox = target.getBoundingBox().inflate(target.getPickRadius() + 0.2D);
            Optional<Vec3> clip = targetBox.clip(start, end);
            if (clip.isPresent() || targetBox.contains(start)) {
                this.hitEntity(target);
            }
        }
    }

    /**
     * 处理命中实体。
     * <p>
     * 冲撞附魔时不对生物造成伤害；否则按飞盘等级与贯穿等级计算伤害，
     * 命中后击退目标、消耗耐久并播放攻击音效。
     *
     * @param target 命中实体
     */
    private void hitEntity(Entity target) {
        this.hitEntities.add(target.getUUID());

        // 冲撞附魔会关闭对生物的伤害，只保留穿行效果。
        if (TDEnchantmentHelper.hasRamming(this.getItem(), this.level())) {
            return;
        }

        if (!(target instanceof LivingEntity living)) {
            return;
        }

        // 基础伤害来自飞盘等级，贯穿附魔每级额外增加 1 点。
        float damage = this.getTier().attackDamage() + TDEnchantmentHelper.getImpalingLevel(this.getItem(), this.level());
        DamageSource source = this.damageSources().thrown(this, this.getOwner());
        if (living.hurt(source, damage)) {
            Vec3 direction = this.getDeltaMovement().normalize();
            living.knockback(0.4D, direction.x, direction.z);
            this.igniteWithFireAspect(living);
            this.damageDisc(1);
            if (!this.level().isClientSide) {
                this.level().playSound(null, living.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 0.45F, 1.35F);
            }
        }
    }

    /**
     * 按火焰附加等级点燃命中的生物。
     * <p>
     * 飞盘是自定义弹射物，不经过原版直接攻击的 post_attack 流程，
     * 因此这里手动读取等级并点燃；每级持续 4 秒，与火焰附加一致。
     *
     * @param target 命中生物
     */
    private void igniteWithFireAspect(LivingEntity target) {
        int level = TDEnchantmentHelper.getFireAspectLevel(this.getItem(), this.level());
        if (level <= 0) {
            return;
        }

        target.igniteForSeconds(4.0F * level);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    14, 0.35D, 0.45D, 0.35D, 0.02D
            );
        }
    }

    /**
     * 在飞行途中留下短促的火焰尾迹。
     */
    private void spawnFireTrail() {
        int level = TDEnchantmentHelper.getFireAspectLevel(this.getItem(), this.level());
        if (level <= 0 || this.tickCount % 2 != 0) {
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.08D, 0.08D, 0.08D, 0.01D
            );
        }
    }

    /**
     * 消耗飞盘耐久。
     * <p>
     * 不可损坏的物品直接忽略；保留附魔会使耐久停在最后 1 点而不损坏，
     * 否则正常扣除耐久，损坏时播放音效、掉落携带物并移除。
     *
     * @param amount 耐久消耗量
     */
    private void damageDisc(int amount) {
        ItemStack stack = this.getItem();
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }

        ItemStack damaged = stack.copy();
        boolean[] broken = {false};
        boolean preserve = TDEnchantmentHelper.hasPreservation(stack, this.level());
        if (this.level() instanceof ServerLevel serverLevel) {
            // 保留附魔：耐久将要耗尽时只留下最后 1 点，不触发损坏。
            if (preserve && damaged.getDamageValue() + amount >= damaged.getMaxDamage()) {
                damaged.setDamageValue(Math.max(0, damaged.getMaxDamage() - 1));
            } else {
                LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
                damaged.hurtAndBreak(amount, serverLevel, owner, item -> broken[0] = true);
            }
        } else {
            damaged.setDamageValue(damaged.getDamageValue() + amount);
            broken[0] = damaged.getDamageValue() >= damaged.getMaxDamage();
            if (preserve && broken[0]) {
                damaged.setDamageValue(Math.max(0, damaged.getMaxDamage() - 1));
                broken[0] = false;
            }
        }

        if (broken[0]) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS, 0.8F, 1.1F);
            this.dropCarriedDrops();
            this.discard();
        } else {
            this.setItem(damaged);
        }
    }

    /**
     * 尝试用冲撞附魔破坏方块。
     * <p>
     * 无冲撞附魔或中心方块不可破坏时返回 false；
     * 否则按扩展等级计算破坏范围（以命中面轴向展开），
     * 逐个破坏可破坏方块并各消耗 1 点耐久。
     *
     * @param hit 方块命中结果
     * @return 破坏了至少一个方块返回 true
     */
    private boolean tryBreakBlock(BlockHitResult hit) {
        if (!TDEnchantmentHelper.hasRamming(this.getItem(), this.level())) {
            return false;
        }

        BlockPos center = hit.getBlockPos();
        // 中心方块不可破坏时仍然反弹，避免飞盘从不可破坏的方块中穿过。
        if (!this.canBreakBlock(center)) {
            return false;
        }

        int radius = TDEnchantmentHelper.getExpansionLevel(this.getItem(), this.level());
        List<BlockPos> targets = new ArrayList<>();

        if (radius <= 0) {
            targets.add(center);
        } else {
            Direction.Axis axis = hit.getDirection().getAxis();
            for (int a = -radius; a <= radius; a++) {
                for (int b = -radius; b <= radius; b++) {
                    BlockPos pos = switch (axis) {
                        case X -> center.offset(0, a, b);
                        case Y -> center.offset(a, 0, b);
                        case Z -> center.offset(a, b, 0);
                    };
                    targets.add(pos);
                }
            }
        }

        boolean broken = false;
        for (BlockPos pos : targets) {
            if (this.isRemoved()) {
                break;
            }
            if (!this.canBreakBlock(pos)) {
                continue;
            }
            if (this.breakBlockAndCollect(pos)) {
                broken = true;
                // 每破坏一个方块消耗 1 点耐久，和工具挖掘保持一致。
                this.damageDisc(1);
            }
        }
        return broken;
    }

    /**
     * 破坏方块并收集掉落物。
     * <p>
     * 无磁返附魔时直接掉落；有磁返附魔时以合适工具采集掉落物，
     * 记入携带列表随飞盘返回。
     *
     * @param pos 方块位置
     * @return 成功破坏返回 true
     */
    private boolean breakBlockAndCollect(BlockPos pos) {
        if (TDEnchantmentHelper.getMagneticReturnLevel(this.getItem(), this.level()) <= 0) {
            return this.level().destroyBlock(pos, true, this, 512);
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockState state = serverLevel.getBlockState(pos);
        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, this, toolFor(state));
        if (!serverLevel.destroyBlock(pos, false, this, 512)) {
            return false;
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                this.carriedDrops.add(drop.copy());
            }
        }
        return true;
    }

    /**
     * 按方块类型选择采集工具。
     *
     * @param state 方块状态
     * @return 对应的下界合金工具，无匹配时返回空堆叠
     */
    private static ItemStack toolFor(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return new ItemStack(Items.NETHERITE_PICKAXE);
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return new ItemStack(Items.NETHERITE_AXE);
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return new ItemStack(Items.NETHERITE_SHOVEL);
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return new ItemStack(Items.NETHERITE_HOE);
        }
        return ItemStack.EMPTY;
    }

    /**
     * 判断方块是否可被冲撞破坏。
     * <p>
     * 空气、液体、不可破坏方块以及当前等级工具无法采集的方块均不可破坏。
     *
     * @param pos 方块位置
     * @return 可破坏返回 true
     */
    private boolean canBreakBlock(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof LiquidBlock) {
            return false;
        }
        if (state.getDestroySpeed(this.level(), pos) < 0.0F) {
            return false;
        }
        return !state.is(this.getTier().breakTier().getIncorrectBlocksForDrops());
    }

    /**
     * 在方块表面反弹。
     * <p>
     * 按命中面法线反射速度并略微减速，贴住命中面后更新返回刻数与距离、
     * 清空命中记录，并播放反弹音效与粒子。
     *
     * @param hit 方块命中结果
     */
    private void bounce(BlockHitResult hit) {
        Vec3 movement = this.getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        Vec3 reflected = movement.subtract(normal.scale(2.0D * movement.dot(normal)));

        if (reflected.lengthSqr() < 1.0E-6D) {
            reflected = normal.scale(0.5D);
        }

        this.setDeltaMovement(reflected.scale(0.96D));
        this.setPos(
                hit.getLocation().x + normal.x * 0.08D,
                hit.getLocation().y + normal.y * 0.08D,
                hit.getLocation().z + normal.z * 0.08D
        );

        this.bounces++;
        this.returnAtTick = Math.min(this.baseFlightTicks() + this.bounces * BOUNCE_TICK_BONUS, MAX_FLIGHT_TICKS);
        this.returnAtDistance = Math.min(this.baseRange() + this.bounces * BOUNCE_RANGE_BONUS, MAX_RANGE);
        this.hitEntities.clear();

        this.level().playSound(null, hit.getBlockPos(), TDSounds.DISC_BOUNCE.get(),
                SoundSource.PLAYERS, 0.9F, 0.95F + this.random.nextFloat() * 0.1F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    6, 0.12D, 0.12D, 0.12D, 0.04D);
        }
    }

    /**
     * 开始返回阶段。
     * <p>
     * 若尚未返回，则设置返回标记、清空命中记录并播放返回音效。
     */
    private void startReturning() {
        if (this.isReturning()) {
            return;
        }

        this.returning = true;
        this.entityData.set(DATA_RETURNING, true);
        this.hitEntities.clear();
        this.level().playSound(null, this.blockPosition(), TDSounds.DISC_RETURN.get(),
                SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    /**
     * 推进返回阶段。
     * <p>
     * 追向投掷者，距离足够近时被接住；投掷者失效时掉落并移除；
     * 返回超时或飞出世界底部时掉落并移除。
     */
    private void tickReturning() {
        Entity owner = this.getOwner();
        if (!(owner instanceof Player player) || !player.isAlive() || player.level() != this.level()) {
            this.dropAndDiscard();
            return;
        }

        Vec3 target = player.position().add(0.0D, player.getEyeHeight() * 0.75D, 0.0D);
        Vec3 toOwner = target.subtract(this.position());
        double distance = toOwner.length();

        if (distance <= CATCH_DISTANCE) {
            this.catchDisc(player);
            return;
        }

        double speed = Math.min(1.25D + distance * 0.045D, 3.2D) * this.returnSpeedMultiplier();
        // 高速返回时不要让飞盘越过玩家，否则会在玩家两侧来回振荡。
        double step = Math.min(speed, distance);
        Vec3 movement = toOwner.normalize().scale(step);
        this.setDeltaMovement(movement);
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        ProjectileUtil.rotateTowardsMovement(this, 1.0F);

        this.returnTicks++;
        if (this.returnTicks > MAX_RETURN_TICKS || this.isBelowWorld()) {
            this.dropAndDiscard();
        }
    }

    /**
     * 接住飞盘。
     * <p>
     * 将飞盘优先放回投掷手，放不下则放入物品栏或掉落；
     * 同时交付携带的掉落物，最后移除实体。
     *
     * @param player 投掷者
     */
    private void catchDisc(Player player) {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            if (player.getItemInHand(this.fromHand).isEmpty()) {
                player.setItemInHand(this.fromHand, stack);
            } else if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }

            this.level().playSound(null, player.blockPosition(), TDSounds.DISC_CATCH.get(),
                    SoundSource.PLAYERS, 0.7F, 1.05F);
        }
        this.deliverCarriedDrops(player);
        this.discard();
    }

    /**
     * 掉落物品并移除实体。
     * <p>
     * 先掉落携带的掉落物，再将飞盘本身作为物品实体掉出。
     */
    private void dropAndDiscard() {
        this.dropCarriedDrops();
        ItemStack stack = this.getItem();
        if (!stack.isEmpty() && !this.level().isClientSide) {
            ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack.copy());
            drop.setPickUpDelay(10);
            this.level().addFreshEntity(drop);
        }
        this.discard();
    }

    /**
     * 将携带的掉落物交给玩家。
     * <p>
     * 放不下的部分掉落在玩家脚下，最后清空携带列表。
     *
     * @param player 玩家
     */
    private void deliverCarriedDrops(Player player) {
        for (ItemStack stack : this.carriedDrops) {
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
        this.carriedDrops.clear();
    }

    /**
     * 掉落携带的掉落物。
     * <p>
     * 客户端不处理；服务端将每个非空掉落物作为物品实体掉出，最后清空列表。
     */
    private void dropCarriedDrops() {
        if (this.level().isClientSide) {
            return;
        }
        for (ItemStack stack : this.carriedDrops) {
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
            }
        }
        this.carriedDrops.clear();
    }

    /**
     * 判断是否已飞出世界底部。
     *
     * @return 飞出世界底部返回 true
     */
    private boolean isBelowWorld() {
        return this.getY() < this.level().getMinBuildHeight() - 32;
    }

    /**
     * 判断能否命中指定实体。
     * <p>
     * 排除投掷者、死亡实体、已命中实体、物品实体与其他飞盘，
     * 且目标必须是可被弹射物命中的活体实体。
     *
     * @param target 目标实体
     * @return 可命中返回 true
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == this.getOwner() || !target.isAlive() || this.hitEntities.contains(target.getUUID())) {
            return false;
        }
        if (target instanceof ItemEntity || target instanceof DiscProjectile) {
            return false;
        }
        return target instanceof LivingEntity && target.canBeHitByProjectile();
    }

    /**
     * 保存附加数据。
     * <p>
     * 写入飞盘物品、返回状态、飞行与返回计数、反弹次数、
     * 携带掉落物与投掷手信息。
     *
     * @param tag 数据标签
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            tag.put("DiscItem", stack.save(this.registryAccess()));
        }
        tag.putBoolean("Returning", this.isReturning());
        tag.putInt("FlightTicks", this.flightTicks);
        tag.putInt("ReturnTicks", this.returnTicks);
        tag.putInt("ReturnAtTick", this.returnAtTick);
        tag.putDouble("ReturnAtDistance", this.returnAtDistance);
        tag.putDouble("Traveled", this.traveled);
        tag.putInt("Bounces", this.bounces);
        ListTag carried = new ListTag();
        for (ItemStack carriedStack : this.carriedDrops) {
            carried.add(carriedStack.save(this.registryAccess()));
        }
        tag.put("CarriedDrops", carried);
        tag.putBoolean("OffHand", this.fromHand == InteractionHand.OFF_HAND);
    }

    /**
     * 读取附加数据。
     * <p>
     * 还原飞盘物品、返回状态、飞行与返回计数、反弹次数、
     * 携带掉落物与投掷手信息。
     *
     * @param tag 数据标签
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DiscItem")) {
            ItemStack.parse(this.registryAccess(), tag.getCompound("DiscItem")).ifPresent(this::setItem);
        }
        this.returning = tag.getBoolean("Returning");
        this.entityData.set(DATA_RETURNING, this.returning);
        this.flightTicks = tag.getInt("FlightTicks");
        this.returnTicks = tag.getInt("ReturnTicks");
        this.returnAtTick = tag.contains("ReturnAtTick") ? tag.getInt("ReturnAtTick") : BASE_FLIGHT_TICKS;
        this.returnAtDistance = tag.contains("ReturnAtDistance") ? tag.getDouble("ReturnAtDistance") : BASE_RANGE;
        this.traveled = tag.getDouble("Traveled");
        this.bounces = tag.getInt("Bounces");
        this.carriedDrops.clear();
        ListTag carried = tag.getList("CarriedDrops", Tag.TAG_COMPOUND);
        for (int i = 0; i < carried.size(); i++) {
            ItemStack.parse(this.registryAccess(), carried.getCompound(i)).ifPresent(this.carriedDrops::add);
        }
        this.fromHand = tag.getBoolean("OffHand") ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
}
