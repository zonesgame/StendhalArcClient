package mindustry.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.EnumSet;
import arc.util.Tmp;
import mindustry.ctype.ContentList;
import mindustry.entities.Damage;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.StaticTree;
import mindustry.world.Tile;
import mindustry.world.blocks.BlockPart;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.DoubleOverlayFloor;
import mindustry.world.blocks.Floor;
import mindustry.world.blocks.OreBlock;
import mindustry.world.blocks.OverlayFloor;
import mindustry.world.blocks.Rock;
import mindustry.world.blocks.StaticWall;
import mindustry.world.blocks.TreeBlock;
import mindustry.world.blocks.defense.DeflectorWall;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.ShockMine;
import mindustry.world.blocks.defense.SurgeWall;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ArtilleryTurret;
import mindustry.world.blocks.defense.turrets.BurstTurret;
import mindustry.world.blocks.defense.turrets.ChargeTurret;
import mindustry.world.blocks.defense.turrets.DoubleTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.distribution.ArmoredConveyor;
import mindustry.world.blocks.distribution.BufferedItemBridge;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.Junction;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.distribution.OverflowGate;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.blocks.distribution.Sorter;
import mindustry.world.blocks.liquid.ArmoredConduit;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBridge;
import mindustry.world.blocks.liquid.LiquidExtendingBridge;
import mindustry.world.blocks.liquid.LiquidJunction;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.liquid.LiquidTank;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.BurnerGenerator;
import mindustry.world.blocks.power.DecayGenerator;
import mindustry.world.blocks.power.ImpactReactor;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.power.NuclearReactor;
import mindustry.world.blocks.power.PowerDiode;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.SingleTypeGenerator;
import mindustry.world.blocks.power.SolarGenerator;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.Cultivator;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Fracker;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericSmelter;
import mindustry.world.blocks.production.Incinerator;
import mindustry.world.blocks.production.LiquidConverter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.production.Separator;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.ItemVoid;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.sandbox.LiquidVoid;
import mindustry.world.blocks.sandbox.PowerSource;
import mindustry.world.blocks.sandbox.PowerVoid;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.LaunchPad;
import mindustry.world.blocks.storage.Unloader;
import mindustry.world.blocks.storage.Vault;
import mindustry.world.blocks.units.CommandCenter;
import mindustry.world.blocks.units.MechPad;
import mindustry.world.blocks.units.RepairPoint;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.modules.LiquidModule;
import z.utils.FinalCons;
import z.utils.ShapeRenderer;
import z.world.blocks.CustomOverlayFloor;
import z.world.blocks.GroundFloor;
import z.world.blocks.SpawnPlayerOverlayFloor;
import z.world.blocks.StaticBlock;
import z.world.blocks.caesar.HousingIso;
import z.world.blocks.caesar.MarketIso;
import z.world.blocks.defense.WallIso;
import z.world.blocks.defense.turrets.ArrowTurretIso;
import z.world.blocks.defense.turrets.StoneThrowerTurretIso;
import z.world.blocks.distribution.ConveyorIso;
import z.world.blocks.distribution.RoadBlockIso;
import z.world.blocks.production.DrillIso;
import z.world.blocks.production.FarmIso;
import z.world.blocks.production.GenericCrafterIso;
import z.world.blocks.storage.GranaryIso;
import z.world.blocks.storage.WarehouseIso;
import z.world.blocks.units.BarracksIso;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

public class Blocks implements ContentList{
    public static Block

    // 环境块. environment
    air, /**重生点*/spawn, deepwater, water, taintedWater, tar, stone, craters, charr, sand, darksand, ice, snow, darksandTaintedWater,
    holostone, rocks, sporerocks, icerocks, cliffs, sporePine, snowPine, pine, shrubs, whiteTree, whiteTreeDead, sporeCluster,
    iceSnow, sandWater, darksandWater, duneRocks, sandRocks, moss, sporeMoss, shale, shaleRocks, shaleBoulder, sandBoulder, grass, salt,
    metalFloor, metalFloorDamaged, metalFloor2, metalFloor3, metalFloor5, ignarock, magmarock, hotrock, snowrocks, rock, snowrock, saltRocks,
    darkPanel1, darkPanel2, darkPanel3, darkPanel4, darkPanel5, darkPanel6, darkMetal,
    pebbles, tendrils,
    // zones add environment begon
    /** 地面*/surface, /** 木材*/timber, /** 粘土*/clay, /** 农场*/farm, /** 海水*/sea, /** 大理石*/marble, /**铁*/iron,
    // 障碍物
    /**树木*/landtree, /**草丛*/landgrass, /**岩石1X*/landrock1x, /**岩石2X*/landrock2x, /**岩石3X*/landrock3x, /**岩石1X高原*/landplateau,
    // zones add environment end

    /** 民居1*/housing1x, /** 民居2*/housing2x,
    // caesar add begon

    // zones add transport begon
    /** 普通道路*/roadConveyor1, /** 辉煌道路*/roadConveyor2, /**路障*/roadConveyorBlock,
    // zones add transport end

    // zones add production begon
    /** 小麦农场*/wheatFarm, /**蔬菜农场*/vegetableFarm, /**水果农场*/fruitFarm, /**橄榄农场*/oliveFarm, /**葡萄农场*/grapesFarm, /**猪肉养殖场*/animalFarm,
    /**大理石采石场*/marbleQuarry, /**铁矿*/ironMine, /** 木材场*/timberYard, /**粘土坑*/clayPit,
    // zones add production end

    // zones add crafting begon
    /** 酿酒厂*/wineWorkshop, /** 炼油厂*/oilWorkshop, /** 武器工厂*/weaponsWorkshop, /**家具厂*/furnitureWorkshop, /** 陶器厂*/potteryWorkshop,
    // zones add crafting end

    // zones add worker begon
    /** 市场*/market,
    // zones add worker end

    // zones add storage begon
    /**仓库*/warehouse1, /**粮仓*/granary1,
    // zones add storage end

    // caesar add end

    // zones add begon
    /**重生单位集合点*/
    rally,
    /** Player场景出生点*/
    spawnPlayer,
//            newDesert, newGrass, newNeutral, newRocky, newSnow,
//    isoTile, isoTile2,

    // 权倾天下数据
    wallIso, arrowTurret, throwerTurret,

    // zones add end

    //矿石. ores
    oreCopper, oreLead, oreScrap, oreCoal, oreTitanium, oreThorium,

    //工厂. crafting
    /**硅冶炼厂*/siliconSmelter, kiln, /**石墨压缩机*/graphitePress, plastaniumCompressor, multiPress, phaseWeaver, surgeSmelter, pyratiteMixer, blastMixer, cryofluidMixer,
    melter, separator, sporePress, pulverizer, incinerator, coalCentrifuge,

    //沙盒. sandbox
    powerSource, powerVoid, itemSource, itemVoid, liquidSource, liquidVoid, message, illuminator,

    //防御. defense
    copperWall, copperWallLarge, titaniumWall, titaniumWallLarge, plastaniumWall, plastaniumWallLarge, thoriumWall, thoriumWallLarge, door, doorLarge,
    phaseWall, phaseWallLarge, surgeWall, surgeWallLarge, mender, mendProjector, overdriveProjector, forceProjector, shockMine,
    scrapWall, scrapWallLarge, scrapWallHuge, scrapWallGigantic, thruster, //ok, these names are getting ridiculous, but at least I don't have humongous walls yet

    //运输. transport
    /**传送带*/conveyor, /**钛传送带*/titaniumConveyor, /**装甲传送带*/armoredConveyor, /***/distributor, /***/junction, /**传送带桥*/itemBridge, /**相知传送带*/phaseConveyor, sorter, invertedSorter, router, overflowGate, underflowGate, massDriver,

    //流体. liquid
    mechanicalPump, rotaryPump, thermalPump, conduit, pulseConduit, platedConduit, liquidRouter, liquidTank, liquidJunction, bridgeConduit, phaseConduit,

    //电力. power
    combustionGenerator, thermalGenerator, turbineGenerator, differentialGenerator, rtgGenerator, solarPanel, largeSolarPanel, thoriumReactor,
    impactReactor, battery, batteryLarge, powerNode, powerNodeLarge, surgeTower, diode,

    //矿机. production
    mechanicalDrill, pneumaticDrill, laserDrill, blastDrill, waterExtractor, oilExtractor, cultivator,

    //储存. storage
    coreShard, coreFoundation, coreNucleus, /**仓库*/vault, container, unloader, launchPad, launchPadLarge,

    //炮塔. turrets
    duo, scatter, scorch, hail, arc, wave, lancer, swarmer, salvo, fuse, ripple, cyclone, spectre, meltdown,

    //单位. units
    /**指挥中心*/commandCenter, /**德鲁格采矿机*/draugFactory, spiritFactory, phantomFactory, wraithFactory, ghoulFactory, revenantFactory, daggerFactory, crawlerFactory, titanFactory,
    fortressFactory, repairPoint,
    // zones add begon
    barracksIso,
    // zones add end

    //升级. upgrades
    dartPad, deltaPad, tauPad, omegaPad, javelinPad, tridentPad, glaivePad;

    @Override
    public void load(){
        //region environment

        air = new Floor("air"){
            {
                alwaysReplace = true;
                hasShadow = false;
            }

            public void draw(Tile tile){}
            public void load(){}
            public void init(){}
            public boolean isHidden(){
                return true;
            }

            public TextureRegion[] variantRegions(){
                if(variantRegions == null){
                    variantRegions = new TextureRegion[]{Core.atlas.find("clear")};
                }
                return variantRegions;
            }
        };

        //create special blockpart variants
        for(int dx = 0; dx < BlockPart.maxSize; dx++){
            for(int dy = 0; dy < BlockPart.maxSize; dy++){
                int fx = dx - BlockPart.maxSize/2, fy = dy - BlockPart.maxSize/2;
                if(fx != 0 || fy != 0){
                    new BlockPart(fx, fy);
                }
            }
        }

        spawn = new OverlayFloor("spawn"){
            {
                variants = 0;
            }
            public void draw(Tile tile){}
        };

        // zones add begon

        //加载xml配置数据
//        XmlReader reader = new XmlReader();
//        XmlReader.Element groundElement = reader.parse("debug/xml/groundSystem.xml");
//        System.out.println(groundElement.toString());

        spawnPlayer = new SpawnPlayerOverlayFloor("spawnPlayer"){
            {
                variants = 0;
            }
            public void draw(Tile tile){}
        };

        rally = new CustomOverlayFloor("rally"){
            {
                variants = 0;
            }
            public void draw(Tile tile){}
        };

//        newDesert = new Floor("newdesert"){{
//            variants = 3;
//        }};
//
//        newGrass = new Floor("newgrass"){{
//            variants = 2;
//        }};
//
//        newNeutral = new Floor("newneutral"){{
//            variants = 4;
//        }};
//
//        newRocky = new Floor("newrocky"){{
//            variants = 4;
//        }};
//
//        newSnow = new Floor("newsnow"){{
//            variants = 3;
//        }};

        {   // test
//            wineWorkshop = new GenericCrafterIso("wineWorkshop", "debug/xml/blocks/wineWorkshop.xml"){{
////            requirements(Category.crafting, ItemStack.with(Items.copper, 1));
//
////            craftEffect = Fx.pulverizeMedium;
////            outputItem = new ItemStack(Items.wine, 1);
////            craftTime = second * 3;
////            size = 2;
////            hasItems = true;
//
////            setItemConsume(new ItemStack(Items.vines, 2));
//
//                drawer = tile -> {
//                    ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
//                };
//            }};
//            System.exit(0);
        }

        // 斜45地块调试数据begon
//        isoTile = new ISOBlock("b1_0_"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 0));
//            health = 300;
//        }};
//        isoTile = new ISOVault("103011"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 20));
//            buildRegionID = "103011";
//            size = 4;
//        }};
//        isoTile2 = new ISOVault("102111"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 40));
//            buildRegionID = "102111";
//            size = 1;
//        }};
        // 斜45地块调试数据end

        // 权倾天下 begon
        wallIso = new WallIso("wallIso", "debug/xml/blocks/wallIso.xml"){{
//            requirements(Category.defense, ItemStack.with(Items.copper, 2));
//            setRequirements( new ItemStack[][]{
//                    ItemStack.with(Items.copper, 10),        // lev1
//                    ItemStack.with(Items.copper, 20),        // lev2
//                    ItemStack.with(Items.copper, 20),        // lev3
//                    ItemStack.with(Items.copper, 30),        // lev4
//                    ItemStack.with(Items.copper, 30),        // lev5
//                    ItemStack.with(Items.copper, 40),        // lev6
//            });
//            levBuildCosts = new float[]{
//                    second * 4,
//                    second * 4,
//                    minute * 1 + second * 10,
//                    minute * 30,
//                    hour * 1 + minute * 10,
//            };
            //  region begon
            // region end
            health[0] = 80 * 4;
        }};

//        duo = new DoubleTurret("duo"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 35), true);
//            ammo(
//                    Items.copper, Bullets.standardCopper,
//                    Items.graphite, Bullets.standardDense,
//                    Items.pyratite, Bullets.standardIncendiary,
//                    Items.silicon, Bullets.standardHoming
//            );
//            reload = 20f;
//            restitution = 0.03f;
//            range = enable_isoInput ? 100f / tilesize : 100;
//            shootCone = 15f;
//            ammoUseEffect = Fx.shellEjectSmall;
//            health = 250;
//            inaccuracy = 2f;
//            rotatespeed = 10f;
//        }};
        // 测试炮塔1
        arrowTurret = new ArrowTurretIso("arrowTurret", "debug/xml/blocks/arrowTurret.xml"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 2), true);
//            ammo(
//                    Items.timber, Bullets.standardCopper,
//                    Items.marble, Bullets.standardDense,
//                    Items.iron, Bullets.standardIncendiary,
//                    Items.weapons, Bullets.standardHoming
//            );
//            reload = 20f;
            restitution = 0.03f;
//            range = enable_isoInput ? 100f / tilesize : 100;
            shootCone = 15f;
            ammoUseEffect = Fx.shellEjectSmall;
//            health[0] = 250;
//            inaccuracy = 2f;
//            rotatespeed = 10f;

            // upgrade begon
//            setRequirements( new ItemStack[][]{
//                    ItemStack.with(Items.copper, 10),        // lev1
//                    ItemStack.with(Items.copper, 20),        // lev2
//                    ItemStack.with(Items.copper, 20),        // lev3
//                    ItemStack.with(Items.copper, 30),        // lev4
//                    ItemStack.with(Items.copper, 30),        // lev5
//                    ItemStack.with(Items.copper, 40),        // lev6
//            });
//            levBuildCosts = new float[]{
//                    second * 4,
//                    second * 4,
//                    minute * 1 + second * 10,
//                    minute * 30,
//                    hour * 1 + minute * 10,
//            };
            //  region begon
//            resourceFoled = new String[]{
//                    "102051", "102052", "102053", "102054", "102055", "102056"
//            };
            // upgrade end
        }};

        // 抛石器
        throwerTurret = new StoneThrowerTurretIso("throwerTurret", "debug/xml/blocks/throwerTurret.xml"){{
//            requirements(Category.turret, ItemStack.with(Items.copper, 150, Items.graphite, 135, Items.titanium, 60));
//            ammo(
//                    Items.graphite, Bullets.artilleryDense,
//                    Items.silicon, Bullets.artilleryHoming,
//                    Items.pyratite, Bullets.artilleryIncendiary,
//                    Items.blastCompound, Bullets.artilleryExplosive,
//                    Items.oil, Bullets.artilleryPlastic
//            );
            size = 2;
            shots[0] = 4;
            inaccuracy[0] = 12f;
            reload[0] = 120f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.shellEjectBig;
            cooldown = 0.03f;
            velocityInaccuracy = 0.2f;
            restitution = 0.02f;
            recoil = 6f;
            shootShake = 2f;
            range[0] = 290f / tilesize;

            health[0] = 130 * size * size;
            shootSound = Sounds.artillery;
        }};

        // 兵营
        barracksIso = new BarracksIso("barracksIso", "debug/xml/blocks/barracks.xml"){{
            requirements(Category.units, ItemStack.with(Items.lead, 55, Items.silicon, 35));
            unitType = UnitTypes.testUnit3;        // dagger
            produceTime = FinalCons.second * 3;
            size = 5;
//            consumes.power(0.5f);
//            consumes.items(new ItemStack(Items.silicon, 6));
            // zones add begon
            needPeasants[0] = 0;
            // zones  add end
        }};

        // 权倾天下 end
        // 环境数据初始化 begon
        surface = new GroundFloor("surface"){{
//            variants = 6;
//            offsetCenter = new int[]{0,0, 0,0, 0,0, 0,0, 0,0, 0,0};
        }};

        timber = new GroundFloor("timber"){{
            itemDrop = Items.timber;
//            variants = 6;
        }};

        clay = new GroundFloor("clay"){{
            itemDrop = Items.clay;
//            variants = 6;
        }};

        farm = new GroundFloor("farmland"){{
            itemDrop = Items.farmland;
//            variants = 6;
        }};

        sea = new GroundFloor("sea"){{
            speedMultiplier = 0.5f;
//            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 90f;
            liquidDrop = Liquids.water;
            isLiquid = true;
//            cacheLayer = CacheLayer.water;
        }};

        marble = new GroundFloor("marble"){{
            itemDrop = Items.marble;
//            variants = 8;
        }};

        iron = new GroundFloor("iron"){{
            itemDrop = Items.iron;
//            variants = 0;
        }};
        // 障碍物初始化
        landtree = new StaticBlock("landtree") {{
//            variants = 33;
            variants = 0;
            // temp begon
            regionsOffset = new Vec2[] {
                    new Vec2(31, 15)
            };
            // temp end
        }};
        landgrass = new StaticBlock("landgrass") {{
            variants = 7;
        }};
        landrock1x = new StaticBlock("landrock1x") {{
            variants = 15;
        }};
        landrock2x = new StaticBlock("landrock2x") {{
            size = 2;
            variants = 6;
        }};
        landrock3x = new StaticBlock("landrock3x") {{
            size = 3;
            variants = 4;
        }};
        landplateau = new StaticBlock("landplateau") {{
            variants = 44;
        }};
        // 环境数据初始化 end

        wheatFarm = new FarmIso("farmWheat", "debug/xml/blocks/farmWheat.xml"){{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            drawMineItem = true;
//            consumes[0].liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.wheat;
//            drillTime = FinalCons.second * size * size * 15 / 10;
            // custom end
        }};
        vegetableFarm = new FarmIso("farmVegetable", "debug/xml/blocks/farmVegetable.xml") {{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.vegetables;
//            drillTime = FinalCons.second * size * size * 15 / 10f;
            // custom end
        }};
        fruitFarm = new FarmIso("farmFruit", "debug/xml/blocks/farmFruit.xml") {{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.fruit;
//            drillTime = FinalCons.second * size * size * 15 / 10f;
            // custom end
        }};
        oliveFarm = new FarmIso("farmOlives", "debug/xml/blocks/farmOlives.xml") {{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.olives;
//            drillTime = FinalCons.second * size * size * 15 / 10f;
            // custom end
        }};
        grapesFarm = new FarmIso("farmGrapes", "debug/xml/blocks/farmGrapes.xml") {{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.vines;
//            drillTime = FinalCons.second * size * size * 15 / 10f;
            // custom end
        }};
        animalFarm = new FarmIso("farmAnimal", "debug/xml/blocks/farmAnimal.xml") {{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 3;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            spawnItem = Items.meat;
//            drillTime = FinalCons.second * size * size * 15 / 10f;
            // custom end
        }};

        roadConveyor1 = new ConveyorIso("roads1x", "debug/xml/blocks/roads1x.xml"){{
//            requirements(Category.distribution, ItemStack.with(Items.copper, 1), true);
//            health[0] = 45;
//            speed = 0.03f;
//            displayedSpeed = 4.2f;
        }};

        roadConveyor2 = new ConveyorIso("roads2x", "debug/xml/blocks/roads2x.xml"){{
//            requirements(Category.distribution, ItemStack.with(Items.copper, 1), true);
//            health[0] = 45;
//            speed = 0.03f;
//            displayedSpeed = 4.2f;
        }};

        roadConveyorBlock = new RoadBlockIso("roadsBlock", "debug/xml/blocks/roadsBlock.xml"){{
//            requirements(Category.distribution, ItemStack.with(Items.copper, 1), true);
//            health[0] = 45;
//            speed = 0.03f;
//            displayedSpeed = 4.2f;
        }};

        warehouse1 = new WarehouseIso("warehouse", "debug/xml/blocks/warehouse.xml"){{
//            requirements(Category.effect, ItemStack.with(Items.copper, 10));
//            size = 3;
//            itemCapacity = 8 * 8;
        }};

        granary1 = new GranaryIso("granary","debug/xml/blocks/granary.xml" ){{
//            requirements(Category.effect, ItemStack.with(Items.copper, 10));
//            size = 3;
//            itemCapacity[0] = 30;
        }};

        // 原料厂 begon
        marbleQuarry = new DrillIso("marbleQuarry", "debug/xml/blocks/marbleQuarry.xml"){{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 2;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            tileItem = Items.marble;
//            drillTime = FinalCons.second * size * size * 10 / 10f;
            // custom end
        }};

        ironMine = new DrillIso("ironMine", "debug/xml/blocks/ironMine.xml"){{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 2;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            tileItem = Items.iron;
//            drillTime[0] = FinalCons.second * size * size * 10 / 10f;
            // custom end
        }};

        timberYard = new DrillIso("timberYard", "debug/xml/blocks/timberYard.xml"){{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 2;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            tileItem = Items.timber;
//            drillTime[0] = FinalCons.second * size * size * 10 / 10f;
            // custom end
        }};

        clayPit = new DrillIso("clayPit", "debug/xml/blocks/clayPit.xml"){{
//            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
//            size = 2;
//            drawMineItem = true;
//            consumes.liquid(Liquids.water, 0.05f).boost();
            // custom begon
//            tileItem = Items.clay;
//            drillTime[0] = FinalCons.second * size * size * 10 / 10f;
            // custom end
        }};
        // 原料厂end

        // 工厂 begon
        wineWorkshop = new GenericCrafterIso("wineWorkshop", "debug/xml/blocks/wineWorkshop.xml"){{
//            requirements(Category.crafting, ItemStack.with(Items.copper, 1));

//            craftEffect = Fx.pulverizeMedium;
//            outputItem = new ItemStack(Items.wine, 1);
//            craftTime = second * 3;
//            size = 2;
//            hasItems = true;

//            setItemConsume(new ItemStack(Items.vines, 2));

            drawer = tile -> {
                ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
            };
        }};
        oilWorkshop = new GenericCrafterIso("oilWorkshop", "debug/xml/blocks/oilWorkshop.xml"){{
//            requirements(Category.crafting, ItemStack.with(Items.copper, 1));

//            craftEffect = Fx.pulverizeMedium;
//            outputItem = new ItemStack(Items.oil, 1);
//            craftTime = second * 3;
//            size = 2;
//            hasItems = true;

//            setItemConsume(new ItemStack(Items.olives, 2));

            drawer = tile -> {
                ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
            };
        }};
        weaponsWorkshop = new GenericCrafterIso("weaponsWorkshop", "debug/xml/blocks/weaponsWorkshop.xml"){{
//            requirements(Category.crafting, ItemStack.with(Items.copper, 1));

//            craftEffect = Fx.pulverizeMedium;
//            outputItem = new ItemStack(Items.weapons, 1);
//            craftTime = second * 3;
//            size = 2;
//            hasItems = true;

//            setItemConsume(new ItemStack(Items.iron, 2), new ItemStack(Items.timber, 2)); //  new ItemStack(Items.silicon, 50), new ItemStack(Items.lead, 30), new ItemStack(Items.titanium, 20)

            drawer = tile -> {
                ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
            };
        }};
        furnitureWorkshop = new GenericCrafterIso("furnitureWorkshop", "debug/xml/blocks/furnitureWorkshop.xml"){{
//            requirements(Category.crafting, ItemStack.with(Items.copper, 75, Items.lead, 30));

//            craftEffect = Fx.pulverizeMedium;
//            outputItem = new ItemStack(Items.furniture, 1);
//            craftTime = second * 3;
//            size = 2;
//            hasItems = true;

//            setItemConsume(new ItemStack(Items.timber, 2));

            drawer = tile -> {
                ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
            };
        }};
        potteryWorkshop = new GenericCrafterIso("potteryWorkshop", "debug/xml/blocks/potteryWorkshop.xml"){{
//            requirements(Category.crafting, ItemStack.with(Items.copper, 1));

//            craftEffect = Fx.pulverizeMedium;
//            outputItem = new ItemStack(Items.pottery, 1);
//            craftTime = second * 3;
//            size = 2;
//            hasItems = true;

//            setItemConsume(new ItemStack(Items.clay, 2), new ItemStack(Items.timber, 2)); //  new ItemStack(Items.silicon, 50), new ItemStack(Items.lead, 30), new ItemStack(Items.titanium, 20)

            drawer = tile -> {
                ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
            };
        }};
        // 工厂 end

        // 民居 begon
        housing1x = new HousingIso("housing1x", "debug/xml/blocks/housing1x.xml"){{
//            requirements(Category.defense, ItemStack.with(Items.copper, 1));
        }};

        housing2x = new HousingIso("housing2x", "debug/xml/blocks/housing2x.xml"){{
//            requirements(Category.defense, ItemStack.with(Items.copper, 1));
        }};
        // 民居 end

        market = new MarketIso("market", "debug/xml/blocks/market.xml"){{
            requirements(Category.effect, ItemStack.with(Items.copper, 10));
//            size = 2;
//            itemCapacity = 8 * 8;
        }};
        // zones add end

        //Registers build blocks
        //no reference is needed here since they can be looked up by name later
        for(int i = 1; i <= BuildBlock.maxSize; i++){
            new BuildBlock(i);
        }

        deepwater = new Floor("deepwater"){{
            speedMultiplier = 0.2f;
            variants = 0;
            liquidDrop = Liquids.water;
            isLiquid = true;
            status = StatusEffects.wet;
            statusDuration = 120f;
            drownTime = 140f;
            cacheLayer = CacheLayer.water;
        }};

        water = new Floor("water"){{
            speedMultiplier = 0.5f;
            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 90f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
        }};

        taintedWater = new Floor("tainted-water"){{
            speedMultiplier = 0.17f;
            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 140f;
            drownTime = 120f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
        }};

        darksandTaintedWater = new Floor("darksand-tainted-water"){{
            speedMultiplier = 0.75f;
            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 60f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
        }};

        sandWater = new Floor("sand-water"){{
            speedMultiplier = 0.8f;
            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 50f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
        }};

        darksandWater = new Floor("darksand-water"){{
            speedMultiplier = 0.8f;
            variants = 0;
            status = StatusEffects.wet;
            statusDuration = 50f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
        }};

        tar = new Floor("tar"){{
            drownTime = 150f;
            status = StatusEffects.tarred;
            statusDuration = 240f;
            speedMultiplier = 0.19f;
            variants = 0;
            liquidDrop = Liquids.oil;
            isLiquid = true;
            cacheLayer = CacheLayer.tar;
        }};

        stone = new Floor("stone"){{

        }};

        craters = new Floor("craters"){{
            variants = 3;
            blendGroup = stone;
        }};

        charr = new Floor("char"){{
            blendGroup = stone;
        }};

        ignarock = new Floor("ignarock"){{

        }};

        hotrock = new Floor("hotrock"){{
            attributes.set(Attribute.heat, 0.5f);
            blendGroup = ignarock;
        }};

        magmarock = new Floor("magmarock"){{
            attributes.set(Attribute.heat, 0.75f);
            updateEffect = Fx.magmasmoke;
            blendGroup = ignarock;
        }};

        sand = new Floor("sand"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
        }};

        darksand = new Floor("darksand"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
        }};

        holostone = new Floor("holostone"){{

        }};

        grass = new Floor("grass"){{

        }};

        salt = new Floor("salt"){{
            variants = 0;
        }};

        snow = new Floor("snow"){{
            attributes.set(Attribute.water, 0.2f);
        }};

        ice = new Floor("ice"){{
            //TODO fix drag/speed
            dragMultiplier = 1f;
            speedMultiplier = 1f;
            attributes.set(Attribute.water, 0.4f);
        }};

        iceSnow = new Floor("ice-snow"){{
            variants = 3;
            attributes.set(Attribute.water, 0.3f);
        }};

        cliffs = new StaticWall("cliffs"){{
            variants = 1;
            fillsTile = false;
        }};

        rocks = new StaticWall("rocks"){{
            variants = 2;
        }};

        sporerocks = new StaticWall("sporerocks"){{
            variants = 2;
        }};

        rock = new Rock("rock"){{
            variants = 2;
        }};

        snowrock = new Rock("snowrock"){{
            variants = 2;
        }};

        icerocks = new StaticWall("icerocks"){{
            variants = 2;
        }};

        snowrocks = new StaticWall("snowrocks"){{
            variants = 2;
        }};

        duneRocks = new StaticWall("dunerocks"){{
            variants = 2;
        }};

        sandRocks = new StaticWall("sandrocks"){{
            variants = 2;
        }};

        saltRocks = new StaticWall("saltrocks"){{
        }};

        sporePine = new StaticTree("spore-pine"){{
            variants = 0;
        }};

        snowPine = new StaticTree("snow-pine"){{
            variants = 0;
        }};

        pine = new StaticTree("pine"){{
            variants = 0;
        }};

        shrubs = new StaticWall("shrubs"){{

        }};

        whiteTreeDead = new TreeBlock("white-tree-dead"){{
        }};

        whiteTree = new TreeBlock("white-tree"){{
        }};

        sporeCluster = new Rock("spore-cluster"){{
            variants = 3;
        }};

        shale = new Floor("shale"){{
            variants = 3;
            attributes.set(Attribute.oil, 0.15f);
        }};

        shaleRocks = new StaticWall("shalerocks"){{
            variants = 2;
        }};

        shaleBoulder = new Rock("shale-boulder"){{
            variants = 2;
        }};

        sandBoulder = new Rock("sand-boulder"){{
            variants = 2;
        }};

        moss = new Floor("moss"){{
            variants = 3;
            attributes.set(Attribute.spores, 0.15f);
        }};

        sporeMoss = new Floor("spore-moss"){{
            variants = 3;
            attributes.set(Attribute.spores, 0.3f);
        }};

        metalFloor = new Floor("metal-floor"){{
            variants = 0;
        }};

        metalFloorDamaged = new Floor("metal-floor-damaged"){{
            variants = 3;
        }};

        metalFloor2 = new Floor("metal-floor-2"){{
            variants = 0;
        }};

        metalFloor3 = new Floor("metal-floor-3"){{
            variants = 0;
        }};

        metalFloor5 = new Floor("metal-floor-5"){{
            variants = 0;
        }};

        darkPanel1 = new Floor("dark-panel-1"){{ variants = 0; }};
        darkPanel2 = new Floor("dark-panel-2"){{ variants = 0; }};
        darkPanel3 = new Floor("dark-panel-3"){{ variants = 0; }};
        darkPanel4 = new Floor("dark-panel-4"){{ variants = 0; }};
        darkPanel5 = new Floor("dark-panel-5"){{ variants = 0; }};
        darkPanel6 = new Floor("dark-panel-6"){{ variants = 0; }};

        darkMetal = new StaticWall("dark-metal");

        pebbles = new DoubleOverlayFloor("pebbles");

        tendrils = new OverlayFloor("tendrils");

        //endregion
        //region ore

        oreCopper = new OreBlock(Items.copper){{
            oreDefault = true;
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};

        oreLead = new OreBlock(Items.lead){{
            oreDefault = true;
            oreThreshold = 0.828f;
            oreScale = 23.952381f;
        }};

        oreScrap = new OreBlock(Items.scrap);

        oreCoal = new OreBlock(Items.coal){{
            oreDefault = true;
            oreThreshold = 0.846f;
            oreScale = 24.428572f;
        }};

        oreTitanium = new OreBlock(Items.titanium){{
            oreDefault = true;
            oreThreshold = 0.864f;
            oreScale = 24.904762f;
        }};

        oreThorium = new OreBlock(Items.thorium){{
            oreDefault = true;
            oreThreshold = 0.882f;
            oreScale = 25.380953f;
        }};

        //endregion
        //region crafting

        graphitePress = new GenericCrafter("graphite-press"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 75, Items.lead, 30));

            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(Items.graphite, 1);
            craftTime = 90f;
            size = 2;
            hasItems = true;

            consumes[0].item(Items.coal, 2);
        }};

        multiPress = new GenericCrafter("multi-press"){{
            requirements(Category.crafting, ItemStack.with(Items.titanium, 100, Items.silicon, 25, Items.lead, 100, Items.graphite, 50));

            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(Items.graphite, 2);
            craftTime = 30f;
            size = 3;
            hasItems = true;
            hasLiquids = true;
            hasPower = true;

            consumes[0].power(1.8f);
            consumes[0].item(Items.coal, 3);
            consumes[0].liquid(Liquids.water, 0.1f);
        }};

        siliconSmelter = new GenericSmelter("silicon-smelter"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 30, Items.lead, 25));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.silicon, 1);
            craftTime = 40f;
            size = 2;
            hasPower = true;
            hasLiquids = false;
            flameColor = Color.valueOf("ffef99");

            consumes[0].items(new ItemStack(Items.coal, 1), new ItemStack(Items.sand, 2));
            consumes[0].power(0.50f);
        }};

        kiln = new GenericSmelter("kiln"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 60, Items.graphite, 30, Items.lead, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.metaglass, 1);
            craftTime = 30f;
            size = 2;
            hasPower = hasItems = true;
            flameColor = Color.valueOf("ffc099");

            consumes[0].items(new ItemStack(Items.lead, 1), new ItemStack(Items.sand, 1));
            consumes[0].power(0.60f);
        }};

        plastaniumCompressor = new GenericCrafter("plastanium-compressor"){{
            requirements(Category.crafting, ItemStack.with(Items.silicon, 80, Items.lead, 115, Items.graphite, 60, Items.titanium, 80));
            hasItems = true;
            liquidCapacity = 60f;
            craftTime = 60f;
            outputItem = new ItemStack(Items.plastanium, 1);
            size = 2;
            health[0] = 320;
            hasPower = hasLiquids = true;
            craftEffect = Fx.formsmoke;
            updateEffect = Fx.plasticburn;

            consumes[0].liquid(Liquids.oil, 0.25f);
            consumes[0].power(3f);
            consumes[0].item(Items.titanium, 2);

            int topRegion = reg("-top");

            drawer = tile -> {
                Draw.rect(region, tile.drawx(), tile.drawy());

                GenericCrafterEntity entity = tile.ent();

                Draw.alpha(Mathf.absin(entity.totalProgress, 3f, 0.9f) * entity.warmup);
                Draw.rect(reg(topRegion), tile.drawx(), tile.drawy());
                Draw.reset();
            };
        }};

        phaseWeaver = new GenericCrafter("phase-weaver"){{
            requirements(Category.crafting, ItemStack.with(Items.silicon, 130, Items.lead, 120, Items.thorium, 75));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.phasefabric, 1);
            craftTime = 120f;
            size = 2;
            hasPower = true;

            consumes[0].items(new ItemStack(Items.thorium, 4), new ItemStack(Items.sand, 10));
            consumes[0].power(5f);
            itemCapacity[0] = 20;

            int bottomRegion = reg("-bottom"), weaveRegion = reg("-weave");

            drawIcons = () -> new TextureRegion[]{Core.atlas.find(name + "-bottom"), Core.atlas.find(name), Core.atlas.find(name + "-weave")};

            drawer = tile -> {
                GenericCrafterEntity entity = tile.ent();

                Draw.rect(reg(bottomRegion), tile.drawx(), tile.drawy());
                Draw.rect(reg(weaveRegion), tile.drawx(), tile.drawy(), entity.totalProgress);

                Draw.color(Pal.accent);
                Draw.alpha(entity.warmup);

                Lines.lineAngleCenter(
                tile.drawx() + Mathf.sin(entity.totalProgress, 6f, tilesize / 3f * size),
                tile.drawy(),
                90,
                size * tilesize / 2f);

                Draw.reset();

                Draw.rect(region, tile.drawx(), tile.drawy());
            };
        }};

        surgeSmelter = new GenericSmelter("alloy-smelter"){{
            requirements(Category.crafting, ItemStack.with(Items.silicon, 80, Items.lead, 80, Items.thorium, 70));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.surgealloy, 1);
            craftTime = 75f;
            size = 3;
            hasPower = true;

            consumes[0].power(4f);
            consumes[0].items(new ItemStack(Items.copper, 3), new ItemStack(Items.lead, 4), new ItemStack(Items.titanium, 2), new ItemStack(Items.silicon, 3));
        }};

        cryofluidMixer = new LiquidConverter("cryofluidmixer"){{
            requirements(Category.crafting, ItemStack.with(Items.lead, 65, Items.silicon, 40, Items.titanium, 60));
            outputLiquid = new LiquidStack(Liquids.cryofluid, 0.2f);
            craftTime = 120f;
            size = 2;
            hasPower = true;
            hasItems = true;
            hasLiquids = true;
            rotate = false;
            solid = true;
            outputsLiquid = true;

            consumes[0].power(1f);
            consumes[0].item(Items.titanium);
            consumes[0].liquid(Liquids.water, 0.2f);

            int liquidRegion = reg("-liquid"), topRegion = reg("-top"), bottomRegion = reg("-bottom");

            drawIcons = () -> new TextureRegion[]{Core.atlas.find(name + "-bottom"), Core.atlas.find(name + "-top")};

            drawer = tile -> {
                LiquidModule mod = tile.entity.liquids;

                int rotation = rotate ? tile.rotation() * 90 : 0;

                Draw.rect(reg(bottomRegion), tile.drawx(), tile.drawy(), rotation);

                if(mod.total() > 0.001f){
                    Draw.color(outputLiquid.liquid.color);
                    Draw.alpha(mod.get(outputLiquid.liquid) / liquidCapacity);
                    Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy(), rotation);
                    Draw.color();
                }

                Draw.rect(reg(topRegion), tile.drawx(), tile.drawy(), rotation);
            };
        }};

        blastMixer = new GenericCrafter("blast-mixer"){{
            requirements(Category.crafting, ItemStack.with(Items.lead, 30, Items.titanium, 20));
            hasItems = true;
            hasPower = true;
            outputItem = new ItemStack(Items.blastCompound, 1);
            size = 2;

            consumes[0].items(new ItemStack(Items.pyratite, 1), new ItemStack(Items.sporePod, 1));
            consumes[0].power(0.40f);
        }};

        pyratiteMixer = new GenericSmelter("pyratite-mixer"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 50, Items.lead, 25));
            flameColor = Color.clear;
            hasItems = true;
            hasPower = true;
            outputItem = new ItemStack(Items.pyratite, 1);

            size = 2;

            consumes[0].power(0.20f);
            consumes[0].items(new ItemStack(Items.coal, 1), new ItemStack(Items.lead, 2), new ItemStack(Items.sand, 2));
        }};

        melter = new GenericCrafter("melter"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 30, Items.lead, 35, Items.graphite, 45));
            health[0] = 200;
            outputLiquid = new LiquidStack(Liquids.slag, 2f);
            craftTime = 10f;
            hasLiquids = hasPower = true;

            consumes[0].power(1f);
            consumes[0].item(Items.scrap, 1);
        }};

        separator = new Separator("separator"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 30, Items.titanium, 25));
            results = ItemStack.with(
            Items.copper, 5,
            Items.lead, 3,
            Items.graphite, 2,
            Items.titanium, 2
            );
            hasPower = true;
            craftTime = 35f;
            size = 2;

            consumes[0].power(1f);
            consumes[0].liquid(Liquids.slag, 0.07f);
        }};

        sporePress = new GenericCrafter("spore-press"){{
            requirements(Category.crafting, ItemStack.with(Items.lead, 35, Items.silicon, 30));
            liquidCapacity = 60f;
            craftTime = 20f;
            outputLiquid = new LiquidStack(Liquids.oil, 6f);
            size = 2;
            health[0] = 320;
            hasLiquids = true;
            hasPower = true;
            craftEffect = Fx.none;

            consumes[0].item(Items.sporePod, 1);
            consumes[0].power(0.60f);

            int[] frameRegions = new int[3];
            for(int i = 0; i < 3; i++){
                frameRegions[i] = reg("-frame" + i);
            }

            int liquidRegion = reg("-liquid");
            int topRegion = reg("-top");

            drawIcons = () -> new TextureRegion[]{Core.atlas.find(name), Core.atlas.find(name + "-top")};
            drawer = tile -> {
                GenericCrafterEntity entity = tile.ent();

                Draw.rect(region, tile.drawx(), tile.drawy());
                Draw.rect(reg(frameRegions[(int)Mathf.absin(entity.totalProgress, 5f, 2.999f)]), tile.drawx(), tile.drawy());
                Draw.color(Color.clear, tile.entity.liquids.current().color, tile.entity.liquids.total() / liquidCapacity);
                Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy());
                Draw.color();
                Draw.rect(reg(topRegion), tile.drawx(), tile.drawy());
            };
        }};

        pulverizer = new GenericCrafter("pulverizer"){{
            requirements(Category.crafting, ItemStack.with(Items.copper, 30, Items.lead, 25));
            outputItem = new ItemStack(Items.sand, 1);
            craftEffect = Fx.pulverize;
            craftTime = 40f;
            updateEffect = Fx.pulverizeSmall;
            hasItems = hasPower = true;

            consumes[0].item(Items.scrap, 1);
            consumes[0].power(0.50f);

            int rotatorRegion = reg("-rotator");

            drawIcons = () -> new TextureRegion[]{Core.atlas.find(name), Core.atlas.find(name + "-rotator")};

            drawer = tile -> {
                GenericCrafterEntity entity = tile.ent();

                Draw.rect(region, tile.drawx(), tile.drawy());
                Draw.rect(reg(rotatorRegion), tile.drawx(), tile.drawy(), entity.totalProgress * 2f);
            };
        }};

        coalCentrifuge = new GenericCrafter("coal-centrifuge"){{
            requirements(Category.crafting, ItemStack.with(Items.titanium, 20, Items.graphite, 40, Items.lead, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.coal, 1);
            craftTime = 30f;
            size = 2;
            hasPower = hasItems = hasLiquids = true;

            consumes[0].liquid(Liquids.oil, 0.09f);
            consumes[0].power(0.5f);
        }};

        incinerator = new Incinerator("incinerator"){{
            requirements(Category.crafting, ItemStack.with(Items.graphite, 5, Items.lead, 15));
            health[0] = 90;
            consumes[0].power(0.50f);
        }};

        //endregion
        //region defense

        int wallHealthMultiplier = 4;

        copperWall = new Wall("copper-wall"){{
            requirements(Category.defense, ItemStack.with(Items.copper, 6));
            health[0] = 80 * wallHealthMultiplier;
        }};

        copperWallLarge = new Wall("copper-wall-large"){{
            requirements(Category.defense, ItemStack.mult(copperWall.requirements, 4));
            health[0] = 80 * 4 * wallHealthMultiplier;
            size = 2;
        }};

        titaniumWall = new Wall("titanium-wall"){{
            requirements(Category.defense, ItemStack.with(Items.titanium, 6));
            health[0] = 110 * wallHealthMultiplier;
        }};

        titaniumWallLarge = new Wall("titanium-wall-large"){{
            requirements(Category.defense, ItemStack.mult(titaniumWall.requirements, 4));
            health[0] = 110 * wallHealthMultiplier * 4;
            size = 2;
        }};

        plastaniumWall = new Wall("plastanium-wall"){{
            requirements(Category.defense, ItemStack.with(Items.plastanium, 5, Items.metaglass, 2));
            health[0] = 190 * wallHealthMultiplier;
            insulated = true;
        }};

        plastaniumWallLarge = new Wall("plastanium-wall-large"){{
            requirements(Category.defense, ItemStack.mult(plastaniumWall.requirements, 4));
            health[0] = 190 * wallHealthMultiplier * 4;
            size = 2;
            insulated = true;
        }};

        thoriumWall = new Wall("thorium-wall"){{
            requirements(Category.defense, ItemStack.with(Items.thorium, 6));
            health[0] = 200 * wallHealthMultiplier;
        }};

        thoriumWallLarge = new Wall("thorium-wall-large"){{
            requirements(Category.defense, ItemStack.mult(thoriumWall.requirements, 4));
            health[0] = 200 * wallHealthMultiplier * 4;
            size = 2;
        }};

        phaseWall = new DeflectorWall("phase-wall"){{
            requirements(Category.defense, ItemStack.with(Items.phasefabric, 6));
            health[0] = 150 * wallHealthMultiplier;
        }};

        phaseWallLarge = new DeflectorWall("phase-wall-large"){{
            requirements(Category.defense, ItemStack.mult(phaseWall.requirements, 4));
            health[0] = 150 * 4 * wallHealthMultiplier;
            size = 2;
        }};

        surgeWall = new SurgeWall("surge-wall"){{
            requirements(Category.defense, ItemStack.with(Items.surgealloy, 6));
            health[0] = 230 * wallHealthMultiplier;
        }};

        surgeWallLarge = new SurgeWall("surge-wall-large"){{
            requirements(Category.defense, ItemStack.mult(surgeWall.requirements, 4));
            health[0] = 230 * 4 * wallHealthMultiplier;
            size = 2;
        }};

        door = new Door("door"){{
            requirements(Category.defense, ItemStack.with(Items.graphite, 6, Items.silicon, 4));
            health[0] = 100 * wallHealthMultiplier;
        }};

        doorLarge = new Door("door-large"){{
            requirements(Category.defense, ItemStack.mult(door.requirements, 4));
            openfx = Fx.dooropenlarge;
            closefx = Fx.doorcloselarge;
            health[0] = 100 * 4 * wallHealthMultiplier;
            size = 2;
        }};

        scrapWall = new Wall("scrap-wall"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with());
            health[0] = 60 * wallHealthMultiplier;
            variants = 5;
        }};

        scrapWallLarge = new Wall("scrap-wall-large"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with());
            health[0] = 60 * 4 * wallHealthMultiplier;
            size = 2;
            variants = 4;
        }};

        scrapWallHuge = new Wall("scrap-wall-huge"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with());
            health[0] = 60 * 9 * wallHealthMultiplier;
            size = 3;
            variants = 3;
        }};

        scrapWallGigantic = new Wall("scrap-wall-gigantic"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with());
            health[0] = 60 * 16 * wallHealthMultiplier;
            size = 4;
        }};

        thruster = new Wall("thruster"){{
            health[0] = 55 * 16 * wallHealthMultiplier;
            size = 4;
        }};

        mender = new MendProjector("mender"){{
            requirements(Category.effect, ItemStack.with(Items.lead, 30, Items.copper, 25));
            consumes[0].power(0.3f);
            size = 1;
            reload = 200f;
            range = 40f;
            healPercent = 4f;
            phaseBoost = 4f;
            phaseRangeBoost = 20f;
            health[0] = 80;
            consumes[0].item(Items.silicon).boost();
        }};

        mendProjector = new MendProjector("mend-projector"){{
            requirements(Category.effect, ItemStack.with(Items.lead, 100, Items.titanium, 25, Items.silicon, 40));
            consumes[0].power(1.5f);
            size = 2;
            reload = 250f;
            range = 85f;
            healPercent = 14f;
            health[0] = 80 * size * size;
            consumes[0].item(Items.phasefabric).boost();
        }};

        overdriveProjector = new OverdriveProjector("overdrive-projector"){{
            requirements(Category.effect, ItemStack.with(Items.lead, 100, Items.titanium, 75, Items.silicon, 75, Items.plastanium, 30));
            consumes[0].power(3.50f);
            size = 2;
            consumes[0].item(Items.phasefabric).boost();
        }};

        forceProjector = new ForceProjector("force-projector"){{
            requirements(Category.effect, ItemStack.with(Items.lead, 100, Items.titanium, 75, Items.silicon, 125));
            size = 3;
            consumes[0].item(Items.phasefabric).boost();
            consumes[0].power(3f);
        }};

        shockMine = new ShockMine("shock-mine"){{
            requirements(Category.effect, ItemStack.with(Items.lead, 25, Items.silicon, 12));
            hasShadow = false;
            health[0] = 40;
            damage = 11;
            tileDamage = 7f;
            length = 10;
            tendrils = 5;
        }};

        //endregion
        //region distribution

        conveyor = new Conveyor("conveyor"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1), true);
            health[0] = 45;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};

        titaniumConveyor = new Conveyor("titanium-conveyor"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1, Items.lead, 1, Items.titanium, 1));
            health[0] = 65;
            speed = 0.08f;
            displayedSpeed = 10f;
        }};

        armoredConveyor = new ArmoredConveyor("armored-conveyor"){{
            requirements(Category.distribution, ItemStack.with(Items.plastanium, 1, Items.thorium, 1, Items.metaglass, 1));
            health[0] = 180;
            speed = 0.08f;
            displayedSpeed = 10f;
        }};

        junction = new Junction("junction"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 2), true);
            speed = 26;
            capacity = 12;
            health[0] = 30;
            buildCostMultiplier = 6f;
        }};

        itemBridge = new BufferedItemBridge("bridge-conveyor"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 4, Items.copper, 4));
            range = 4;
            speed = 70f;
            bufferCapacity = 14;
        }};

        phaseConveyor = new ItemBridge("phase-conveyor"){{
            requirements(Category.distribution, ItemStack.with(Items.phasefabric, 5, Items.silicon, 7, Items.lead, 10, Items.graphite, 10));
            range = 12;
            canOverdrive = false;
            hasPower = true;
            consumes[0].power(0.30f);
        }};

        sorter = new Sorter("sorter"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 2, Items.copper, 2));
            buildCostMultiplier = 3f;
        }};

        invertedSorter = new Sorter("inverted-sorter"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 2, Items.copper, 2));
            buildCostMultiplier = 3f;
            invert = true;
        }};

        router = new Router("router"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 3));
            buildCostMultiplier = 2f;
        }};

        distributor = new Router("distributor"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 4, Items.copper, 4));
            size = 2;
        }};

        overflowGate = new OverflowGate("overflow-gate"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 2, Items.copper, 4));
            buildCostMultiplier = 3f;
        }};

        underflowGate = new OverflowGate("underflow-gate"){{
            requirements(Category.distribution, ItemStack.with(Items.lead, 2, Items.copper, 4));
            buildCostMultiplier = 3f;
            invert = true;
        }};

        massDriver = new MassDriver("mass-driver"){{
            requirements(Category.distribution, ItemStack.with(Items.titanium, 125, Items.silicon, 75, Items.lead, 125, Items.thorium, 50));
            size = 3;
            itemCapacity[0] = 120;
            reloadTime = 200f;
            range = 440f;
            consumes[0].power(1.75f);
        }};

        //endregion
        //region liquid

        mechanicalPump = new Pump("mechanical-pump"){{
            requirements(Category.liquid, ItemStack.with(Items.copper, 15, Items.metaglass, 10));
            pumpAmount = 0.1f;
        }};

        rotaryPump = new Pump("rotary-pump"){{
            requirements(Category.liquid, ItemStack.with(Items.copper, 70, Items.metaglass, 50, Items.silicon, 20, Items.titanium, 35));
            pumpAmount = 0.8f;
            consumes[0].power(0.15f);
            liquidCapacity = 30f;
            hasPower = true;
            size = 2;
        }};

        thermalPump = new Pump("thermal-pump"){{
            requirements(Category.liquid, ItemStack.with(Items.copper, 80, Items.metaglass, 70, Items.silicon, 30, Items.titanium, 40, Items.thorium, 35));
            pumpAmount = 1.5f;
            consumes[0].power(0.30f);
            liquidCapacity = 40f;
            hasPower = true;
            size = 3;
        }};

        conduit = new Conduit("conduit"){{
            requirements(Category.liquid, ItemStack.with(Items.metaglass, 1));
            health[0] = 45;
        }};

        pulseConduit = new Conduit("pulse-conduit"){{
            requirements(Category.liquid, ItemStack.with(Items.titanium, 2, Items.metaglass, 1));
            liquidCapacity = 16f;
            liquidPressure = 1.025f;
            health[0] = 90;
        }};

        platedConduit = new ArmoredConduit("plated-conduit"){{
            requirements(Category.liquid, ItemStack.with(Items.thorium, 2, Items.metaglass, 1, Items.plastanium, 1));
            liquidCapacity = 16f;
            liquidPressure = 1.025f;
            health[0] = 220;
        }};

        liquidRouter = new LiquidRouter("liquid-router"){{
            requirements(Category.liquid, ItemStack.with(Items.graphite, 4, Items.metaglass, 2));
            liquidCapacity = 20f;
        }};

        liquidTank = new LiquidTank("liquid-tank"){{
            requirements(Category.liquid, ItemStack.with(Items.titanium, 25, Items.metaglass, 25));
            size = 3;
            liquidCapacity = 1500f;
            health[0] = 500;
        }};

        liquidJunction = new LiquidJunction("liquid-junction"){{
            requirements(Category.liquid, ItemStack.with(Items.graphite, 2, Items.metaglass, 2));
        }};

        bridgeConduit = new LiquidExtendingBridge("bridge-conduit"){{
            requirements(Category.liquid, ItemStack.with(Items.graphite, 4, Items.metaglass, 8));
            range = 4;
            hasPower = false;
        }};

        phaseConduit = new LiquidBridge("phase-conduit"){{
            requirements(Category.liquid, ItemStack.with(Items.phasefabric, 5, Items.silicon, 7, Items.metaglass, 20, Items.titanium, 10));
            range = 12;
            hasPower = true;
            canOverdrive = false;
            consumes[0].power(0.30f);
        }};

        //endregion
        //region power

        powerNode = new PowerNode("power-node"){{
            requirements(Category.power, ItemStack.with(Items.copper, 1, Items.lead, 3));
            maxNodes = 20;
            laserRange = 6;
        }};

        powerNodeLarge = new PowerNode("power-node-large"){{
            requirements(Category.power, ItemStack.with(Items.titanium, 5, Items.lead, 10, Items.silicon, 3));
            size = 2;
            maxNodes = 30;
            laserRange = 9.5f;
        }};

        surgeTower = new PowerNode("surge-tower"){{
            requirements(Category.power, ItemStack.with(Items.titanium, 7, Items.lead, 10, Items.silicon, 15, Items.surgealloy, 15));
            size = 2;
            maxNodes = 2;
            laserRange = 30f;
        }};

        diode = new PowerDiode("diode"){{
            requirements(Category.power, ItemStack.with(Items.silicon, 10, Items.plastanium, 5, Items.metaglass, 10));
        }};

        battery = new Battery("battery"){{
            requirements(Category.power, ItemStack.with(Items.copper, 4, Items.lead, 20));
            consumes[0].powerBuffered(4000f);
        }};

        batteryLarge = new Battery("battery-large"){{
            requirements(Category.power, ItemStack.with(Items.titanium, 20, Items.lead, 40, Items.silicon, 20));
            size = 3;
            consumes[0].powerBuffered(50000f);
        }};

        combustionGenerator = new BurnerGenerator("combustion-generator"){{
            requirements(Category.power, ItemStack.with(Items.copper, 25, Items.lead, 15));
            powerProduction = 1f;
            itemDuration = 120f;
        }};

        thermalGenerator = new ThermalGenerator("thermal-generator"){{
            requirements(Category.power, ItemStack.with(Items.copper, 40, Items.graphite, 35, Items.lead, 50, Items.silicon, 35, Items.metaglass, 40));
            powerProduction = 1.8f;
            generateEffect = Fx.redgeneratespark;
            size = 2;
        }};

        turbineGenerator = new BurnerGenerator("turbine-generator"){{
            requirements(Category.power, ItemStack.with(Items.copper, 35, Items.graphite, 25, Items.lead, 40, Items.silicon, 30));
            powerProduction = 6f;
            itemDuration = 90f;
            consumes[0].liquid(Liquids.water, 0.05f);
            hasLiquids = true;
            size = 2;
        }};

        differentialGenerator = new SingleTypeGenerator("differential-generator"){{
            requirements(Category.power, ItemStack.with(Items.copper, 70, Items.titanium, 50, Items.lead, 100, Items.silicon, 65, Items.metaglass, 50));
            powerProduction = 16f;
            itemDuration = 140f;
            hasLiquids = true;
            hasItems = true;
            size = 3;

            consumes[0].item(Items.pyratite).optional(true, false);
            consumes[0].liquid(Liquids.cryofluid, 0.15f);
        }};

        rtgGenerator = new DecayGenerator("rtg-generator"){{
            requirements(Category.power, ItemStack.with(Items.lead, 100, Items.silicon, 75, Items.phasefabric, 25, Items.plastanium, 75, Items.thorium, 50));
            size = 2;
            powerProduction = 3f;
            itemDuration = 440f;
        }};

        solarPanel = new SolarGenerator("solar-panel"){{
            requirements(Category.power, ItemStack.with(Items.lead, 10, Items.silicon, 15));
            powerProduction = 0.06f;
        }};

        largeSolarPanel = new SolarGenerator("solar-panel-large"){{
            requirements(Category.power, ItemStack.with(Items.lead, 100, Items.silicon, 145, Items.phasefabric, 15));
            size = 3;
            powerProduction = 0.9f;
        }};

        thoriumReactor = new NuclearReactor("thorium-reactor"){{
            requirements(Category.power, ItemStack.with(Items.lead, 300, Items.silicon, 200, Items.graphite, 150, Items.thorium, 150, Items.metaglass, 50));
            size = 3;
            health[0] = 700;
            itemDuration = 360f;
            powerProduction = 14f;
            consumes[0].item(Items.thorium);
            heating = 0.02f;
            consumes[0].liquid(Liquids.cryofluid, heating / coolantPower).update(false);
        }};

        impactReactor = new ImpactReactor("impact-reactor"){{
            requirements(Category.power, ItemStack.with(Items.lead, 500, Items.silicon, 300, Items.graphite, 400, Items.thorium, 100, Items.surgealloy, 250, Items.metaglass, 250));
            size = 4;
            health[0] = 900;
            powerProduction = 130f;
            itemDuration = 140f;
            consumes[0].power(25f);
            consumes[0].item(Items.blastCompound);
            consumes[0].liquid(Liquids.cryofluid, 0.25f);
        }};

        //endregion power
        //region production

        mechanicalDrill = new Drill("mechanical-drill"){{
            requirements(Category.production, ItemStack.with(Items.copper, 12), true);
            tier = 2;
            drillTime = 600;
            size = 2;
            drawMineItem = true;
            consumes[0].liquid(Liquids.water, 0.05f).boost();
        }};

        pneumaticDrill = new Drill("pneumatic-drill"){{
            requirements(Category.production, ItemStack.with(Items.copper, 18, Items.graphite, 10));
            tier = 3;
            drillTime = 400;
            size = 2;
            drawMineItem = true;
            consumes[0].liquid(Liquids.water, 0.06f).boost();
        }};

        laserDrill = new Drill("laser-drill"){{
            requirements(Category.production, ItemStack.with(Items.copper, 35, Items.graphite, 30, Items.silicon, 30, Items.titanium, 20));
            drillTime = 280;
            size = 3;
            hasPower = true;
            tier = 4;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;

            consumes[0].power(1.10f);
            consumes[0].liquid(Liquids.water, 0.08f).boost();
        }};

        blastDrill = new Drill("blast-drill"){{
            requirements(Category.production, ItemStack.with(Items.copper, 65, Items.silicon, 60, Items.titanium, 50, Items.thorium, 75));
            drillTime = 280;
            size = 4;
            drawRim = true;
            hasPower = true;
            tier = 5;
            updateEffect = Fx.pulverizeRed;
            updateEffectChance = 0.03f;
            drillEffect = Fx.mineHuge;
            rotateSpeed = 6f;
            warmupSpeed = 0.01f;

            consumes[0].power(3f);
            consumes[0].liquid(Liquids.water, 0.1f).boost();
        }};

        waterExtractor = new SolidPump("water-extractor"){{
            requirements(Category.production, ItemStack.with(Items.copper, 25, Items.graphite, 25, Items.lead, 20));
            result = Liquids.water;
            pumpAmount = 0.13f;
            size = 2;
            liquidCapacity = 30f;
            rotateSpeed = 1.4f;
            attribute = Attribute.water;

            consumes[0].power(1f);
        }};

        cultivator = new Cultivator("cultivator"){{
            requirements(Category.production, ItemStack.with(Items.copper, 10, Items.lead, 25, Items.silicon, 10));
            outputItem = new ItemStack(Items.sporePod, 1);
            craftTime = 140;
            size = 2;
            hasLiquids = true;
            hasPower = true;
            hasItems = true;

            consumes[0].power(0.80f);
            consumes[0].liquid(Liquids.water, 0.18f);
        }};

        oilExtractor = new Fracker("oil-extractor"){{
            requirements(Category.production, ItemStack.with(Items.copper, 150, Items.graphite, 175, Items.lead, 115, Items.thorium, 115, Items.silicon, 75));
            result = Liquids.oil;
            updateEffect = Fx.pulverize;
            liquidCapacity = 50f;
            updateEffectChance = 0.05f;
            pumpAmount = 0.25f;
            size = 3;
            liquidCapacity = 30f;
            attribute = Attribute.oil;

            consumes[0].item(Items.sand);
            consumes[0].power(3f);
            consumes[0].liquid(Liquids.water, 0.15f);
        }};

        //endregion
        //region storage

        coreShard = new CoreBlock("core-shard"){{
            requirements(Category.effect, BuildVisibility.debugOnly, ItemStack.with());
            alwaysUnlocked = true;

            health[0] = 1100;
            itemCapacity[0] = 4000;
            size = 3;
        }};

        coreFoundation = new CoreBlock("core-foundation"){{
            requirements(Category.effect, BuildVisibility.debugOnly, ItemStack.with());

            health[0] = 2000;
            itemCapacity[0] = 9000;
            size = 4;
        }};

        coreNucleus = new CoreBlock("core-nucleus"){{
            requirements(Category.effect, BuildVisibility.debugOnly, ItemStack.with());

            health[0] = 4000;
            itemCapacity[0] = 13000;
            size = 5;
        }};

        vault = new Vault("vault"){{
            requirements(Category.effect, ItemStack.with(Items.titanium, 250, Items.thorium, 125));
            size = 3;
            itemCapacity[0] = 1000;
        }};

        container = new Vault("container"){{
            requirements(Category.effect, ItemStack.with(Items.titanium, 100));
            size = 2;
            itemCapacity[0] = 300;
        }};

        unloader = new Unloader("unloader"){{
            requirements(Category.effect, ItemStack.with(Items.titanium, 25, Items.silicon, 30));
            speed = 7f;
        }};

        launchPad = new LaunchPad("launch-pad"){{
            requirements(Category.effect, BuildVisibility.campaignOnly, ItemStack.with(Items.copper, 250, Items.silicon, 75, Items.lead, 100));
            size = 3;
            itemCapacity[0] = 100;
            launchTime = 60f * 16;
            hasPower = true;
            consumes[0].power(1f);
        }};

        launchPadLarge = new LaunchPad("launch-pad-large"){{
            requirements(Category.effect, BuildVisibility.campaignOnly, ItemStack.with(Items.titanium, 200, Items.silicon, 150, Items.lead, 250, Items.plastanium, 75));
            size = 4;
            itemCapacity[0] = 250;
            launchTime = 60f * 14;
            hasPower = true;
            consumes[0].power(2f);
        }};


        //endregion
        //region turrets

        duo = new DoubleTurret("duo"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 35), true);
            ammo(
            Items.copper, Bullets.standardCopper,
            Items.graphite, Bullets.standardDense,
            Items.pyratite, Bullets.standardIncendiary,
            Items.silicon, Bullets.standardHoming
            );
            reload = 20f;
            restitution = 0.03f;
            range = enable_isoInput ? 100f / tilesize : 100;
            shootCone = 15f;
            ammoUseEffect = Fx.shellEjectSmall;
            health[0] = 250;
            inaccuracy = 2f;
            rotatespeed = 10f;
        }};

        scatter = new BurstTurret("scatter"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 85, Items.lead, 45));
            ammo(
            Items.scrap, Bullets.flakScrap,
            Items.lead, Bullets.flakLead,
            Items.metaglass, Bullets.flakGlass
            );
            reload = 18f;
            range = 170f;
            size = 2;
            burstSpacing = 5f;
            shots = 2;
            targetGround = false;

            recoil = 2f;
            rotatespeed = 15f;
            inaccuracy = 17f;
            shootCone = 35f;

            health[0] = 200 * size * size;
            shootSound = Sounds.shootSnap;
        }};

        scorch = new ItemTurret("scorch"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 25, Items.graphite, 22));
            ammo(
            Items.coal, Bullets.basicFlame,
            Items.pyratite, Bullets.pyraFlame
            );
            recoil = 0f;
            reload = 5f;
            coolantMultiplier = 2f;
            range = 60f;
            shootCone = 50f;
            targetAir = false;
            ammoUseEffect = Fx.none;
            health[0] = 400;
            shootSound = Sounds.flame;
        }};

        hail = new ArtilleryTurret("hail"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 40, Items.graphite, 17));
            ammo(
            Items.graphite, Bullets.artilleryDense,
            Items.silicon, Bullets.artilleryHoming,
            Items.pyratite, Bullets.artilleryIncendiary
            );
            reload = 60f;
            recoil = 2f;
            range = 230f;
            inaccuracy = 1f;
            shootCone = 10f;
            health[0] = 260;
            shootSound = Sounds.artillery;
        }};

        wave = new LiquidTurret("wave"){{
            requirements(Category.turret, ItemStack.with(Items.metaglass, 45, Items.lead, 75));
            ammo(
            Liquids.water, Bullets.waterShot,
            Liquids.slag, Bullets.slagShot,
            Liquids.cryofluid, Bullets.cryoShot,
            Liquids.oil, Bullets.oilShot
            );
            size = 2;
            recoil = 0f;
            reload = 2f;
            inaccuracy = 5f;
            shootCone = 50f;
            shootEffect = Fx.shootLiquid;
            range = 110f;
            health[0] = 250 * size * size;
            shootSound = Sounds.splash;
        }};

        lancer = new ChargeTurret("lancer"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 25, Items.lead, 50, Items.silicon, 45));
            range = 155f;
            chargeTime = 50f;
            chargeMaxDelay = 30f;
            chargeEffects = 7;
            shootType = Bullets.lancerLaser;
            recoil = 2f;
            reload = 90f;
            cooldown = 0.03f;
            powerUse = 2.5f;
            shootShake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.lancerLaserShootSmoke;
            chargeEffect = Fx.lancerLaserCharge;
            chargeBeginEffect = Fx.lancerLaserChargeBegin;
            heatColor = Color.red;
            size = 2;
            health[0] = 280 * size * size;
            targetAir = false;
            shootSound = Sounds.laser;
        }};

        arc = new PowerTurret("arc"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 35, Items.lead, 50));
            shootType = Bullets.arc;
            reload = 35f;
            shootCone = 40f;
            rotatespeed = 8f;
            powerUse = 1.5f;
            targetAir = false;
            range = 90f;
            shootEffect = Fx.lightningShoot;
            heatColor = Color.red;
            recoil = 1f;
            size = 1;
            health[0] = 260;
            shootSound = Sounds.spark;
        }};

        swarmer = new BurstTurret("swarmer"){{
            requirements(Category.turret, ItemStack.with(Items.graphite, 35, Items.titanium, 35, Items.plastanium, 45, Items.silicon, 30));
            ammo(
            Items.blastCompound, Bullets.missileExplosive,
            Items.pyratite, Bullets.missileIncendiary,
            Items.surgealloy, Bullets.missileSurge
            );
            reload = 40f;
            shots = 4;
            burstSpacing = 5;
            inaccuracy = 10f;
            range = 185f;
            xRand = 6f;
            size = 2;
            health[0] = 300 * size * size;
            shootSound = Sounds.missile;
        }};

        salvo = new BurstTurret("salvo"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 105, Items.graphite, 95, Items.titanium, 60));
            ammo(
            Items.copper, Bullets.standardCopper,
            Items.graphite, Bullets.standardDense,
            Items.pyratite, Bullets.standardIncendiary,
            Items.silicon, Bullets.standardHoming,
            Items.thorium, Bullets.standardThorium
            );

            size = 2;
            range = 150f;
            reload = 38f;
            restitution = 0.03f;
            ammoEjectBack = 3f;
            cooldown = 0.03f;
            recoil = 3f;
            shootShake = 2f;
            burstSpacing = 3f;
            shots = 4;
            ammoUseEffect = Fx.shellEjectBig;
            health[0] = 240 * size * size;
            shootSound = Sounds.shootBig;
        }};

        fuse = new ItemTurret("fuse"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 225, Items.graphite, 225, Items.thorium, 100));

            reload = 35f;
            shootShake = 4f;
            range = 90f;
            recoil = 5f;
            shots = 3;
            spread = 20f;
            restitution = 0.1f;
            shootCone = 30;
            size = 3;

            health[0] = 220 * size * size;
            shootSound = Sounds.shotgun;

            ammo(Items.graphite, new BulletType(0.01f, 105){
                int rays = 1;
                float rayLength = range + 10f;

                {
                    hitEffect = Fx.hitLancer;
                    shootEffect = smokeEffect = Fx.lightningShoot;
                    lifetime = 10f;
                    despawnEffect = Fx.none;
                    pierce = true;
                }

                @Override
                public void init(mindustry.entities.type.Bullet b){
                    for(int i = 0; i < rays; i++){
                        Damage.collideLine(b, b.getTeam(), hitEffect, b.x, b.y, b.rot(), rayLength - Math.abs(i - (rays / 2)) * 20f);
                    }
                }

                @Override
                public void draw(Bullet b){
                    super.draw(b);
                    Draw.color(Color.white, Pal.lancerLaser, b.fin());
                    //Draw.alpha(b.fout());
                    for(int i = 0; i < 7; i++){
                        Tmp.v1.trns(b.rot(), i * 8f);
                        float sl = Mathf.clamp(b.fout() - 0.5f) * (80f - i * 10);
                        Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 4f, sl, b.rot() + 90);
                        Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 4f, sl, b.rot() - 90);
                    }
                    Drawf.tri(b.x, b.y, 20f * b.fout(), (rayLength + 50), b.rot());
                    Drawf.tri(b.x, b.y, 20f * b.fout(), 10f, b.rot() + 180f);
                    Draw.reset();
                }
            });
        }};

        ripple = new ArtilleryTurret("ripple"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 150, Items.graphite, 135, Items.titanium, 60));
            ammo(
            Items.graphite, Bullets.artilleryDense,
            Items.silicon, Bullets.artilleryHoming,
            Items.pyratite, Bullets.artilleryIncendiary,
            Items.blastCompound, Bullets.artilleryExplosive,
            Items.plastanium, Bullets.artilleryPlastic
            );
            size = 3;
            shots = 4;
            inaccuracy = 12f;
            reload = 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.shellEjectBig;
            cooldown = 0.03f;
            velocityInaccuracy = 0.2f;
            restitution = 0.02f;
            recoil = 6f;
            shootShake = 2f;
            range = 290f;

            health[0] = 130 * size * size;
            shootSound = Sounds.artillery;
        }};

        cyclone = new ItemTurret("cyclone"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 200, Items.titanium, 125, Items.plastanium, 80));
            ammo(
            Items.metaglass, Bullets.flakGlass,
            Items.blastCompound, Bullets.flakExplosive,
            Items.plastanium, Bullets.flakPlastic,
            Items.surgealloy, Bullets.flakSurge
            );
            xRand = 4f;
            reload = 6f;
            range = 200f;
            size = 3;
            recoil = 3f;
            rotatespeed = 10f;
            inaccuracy = 10f;
            shootCone = 30f;
            shootSound = Sounds.shootSnap;

            health[0] = 145 * size * size;
        }};

        spectre = new DoubleTurret("spectre"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 350, Items.graphite, 300, Items.surgealloy, 250, Items.plastanium, 175, Items.thorium, 250));
            ammo(
            Items.graphite, Bullets.standardDenseBig,
            Items.pyratite, Bullets.standardIncendiaryBig,
            Items.thorium, Bullets.standardThoriumBig
            );
            reload = 6f;
            coolantMultiplier = 0.5f;
            restitution = 0.1f;
            ammoUseEffect = Fx.shellEjectBig;
            range = 200f;
            inaccuracy = 3f;
            recoil = 3f;
            xRand = 3f;
            shotWidth = 4f;
            shootShake = 2f;
            shots = 2;
            size = 4;
            shootCone = 24f;
            shootSound = Sounds.shootBig;

            health[0] = 155 * size * size;
            consumes[0].add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 2f)).update(false).optional(true, true);
        }};

        meltdown = new LaserTurret("meltdown"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 250, Items.lead, 350, Items.graphite, 300, Items.surgealloy, 325, Items.silicon, 325));
            shootType = Bullets.meltdownLaser;
            shootEffect = Fx.shootBigSmoke2;
            shootCone = 40f;
            recoil = 4f;
            size = 4;
            shootShake = 2f;
            range = 190f;
            reload = 80f;
            firingMoveFract = 0.5f;
            shootDuration = 220f;
            powerUse = 14f;
            shootSound = Sounds.laserbig;
            activeSound = Sounds.beam;
            activeSoundVolume = 2f;

            health[0] = 200 * size * size;
            consumes[0].add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.5f)).update(false);
        }};

        //endregion
        //region units

        draugFactory = new UnitFactory("draug-factory"){{
            requirements(Category.units, ItemStack.with(Items.copper, 30, Items.lead, 70));
            unitType = UnitTypes.draug;
            produceTime = 2500;
            size = 2;
            maxSpawn = 1;
            consumes[0].power(1.2f);
            consumes[0].items();
        }};

        spiritFactory = new UnitFactory("spirit-factory"){{
            requirements(Category.units, ItemStack.with(Items.metaglass, 45, Items.lead, 55, Items.silicon, 45));
            unitType = UnitTypes.spirit;
            produceTime = 4000;
            size = 2;
            maxSpawn = 1;
            consumes[0].power(1.2f);
            consumes[0].items(new ItemStack(Items.silicon, 30), new ItemStack(Items.lead, 30));
        }};

        phantomFactory = new UnitFactory("phantom-factory"){{
            requirements(Category.units, ItemStack.with(Items.titanium, 50, Items.thorium, 60, Items.lead, 65, Items.silicon, 105));
            unitType = UnitTypes.phantom;
            produceTime = 4400;
            size = 2;
            maxSpawn = 1;
            consumes[0].power(2.5f);
            consumes[0].items(new ItemStack(Items.silicon, 50), new ItemStack(Items.lead, 30), new ItemStack(Items.titanium, 20));
        }};

        commandCenter = new CommandCenter("command-center"){{
            requirements(Category.units, ItemStack.with(Items.copper, 200, Items.lead, 250, Items.silicon, 250, Items.graphite, 100));
            flags = EnumSet.of(BlockFlag.rally, BlockFlag.comandCenter);
            size = 2;
            health[0] = size * size * 55;
        }};

        wraithFactory = new UnitFactory("wraith-factory"){{
            requirements(Category.units, ItemStack.with(Items.titanium, 30, Items.lead, 40, Items.silicon, 45));
            unitType = UnitTypes.wraith;
            produceTime = 700;
            size = 2;
            consumes[0].power(0.5f);
            consumes[0].items(new ItemStack(Items.silicon, 10), new ItemStack(Items.titanium, 5));
        }};

        ghoulFactory = new UnitFactory("ghoul-factory"){{
            requirements(Category.units, ItemStack.with(Items.titanium, 75, Items.lead, 65, Items.silicon, 110));
            unitType = UnitTypes.ghoul;
            produceTime = 1150;
            size = 3;
            consumes[0].power(1.2f);
            consumes[0].items(new ItemStack(Items.silicon, 15), new ItemStack(Items.titanium, 10));
        }};

        revenantFactory = new UnitFactory("revenant-factory"){{
            requirements(Category.units, ItemStack.with(Items.plastanium, 50, Items.titanium, 150, Items.lead, 150, Items.silicon, 200));
            unitType = UnitTypes.revenant;
            produceTime = 2000;
            size = 4;
            consumes[0].power(3f);
            consumes[0].items(new ItemStack(Items.silicon, 40), new ItemStack(Items.titanium, 30));
        }};

        daggerFactory = new UnitFactory("dagger-factory"){{
            requirements(Category.units, ItemStack.with(Items.lead, 55, Items.silicon, 35));
            unitType = UnitTypes.dagger;
            produceTime = 850;
            size = 2;
            consumes[0].power(0.5f);
            consumes[0].items(new ItemStack(Items.silicon, 6));
        }};

        crawlerFactory = new UnitFactory("crawler-factory"){{
            requirements(Category.units, ItemStack.with(Items.lead, 45, Items.silicon, 30));
            unitType = UnitTypes.crawler;
            produceTime = 300;
            size = 2;
            maxSpawn = 6;
            consumes[0].power(0.5f);
            consumes[0].items(new ItemStack(Items.coal, 10));
        }};

        titanFactory = new UnitFactory("titan-factory"){{
            requirements(Category.units, ItemStack.with(Items.graphite, 50, Items.lead, 50, Items.silicon, 45));
            unitType = UnitTypes.titan;
            produceTime = 1050;
            size = 3;
            consumes[0].power(0.60f);
            consumes[0].items(new ItemStack(Items.silicon, 12));
        }};

        fortressFactory = new UnitFactory("fortress-factory"){{
            requirements(Category.units, ItemStack.with(Items.thorium, 40, Items.lead, 110, Items.silicon, 75));
            unitType = UnitTypes.fortress;
            produceTime = 2000;
            size = 3;
            maxSpawn = 3;
            consumes[0].power(1.4f);
            consumes[0].items(new ItemStack(Items.silicon, 20), new ItemStack(Items.graphite, 10));
        }};

        repairPoint = new RepairPoint("repair-point"){{
            requirements(Category.units, ItemStack.with(Items.lead, 15, Items.copper, 15, Items.silicon, 15));
            repairSpeed = 0.5f;
            repairRadius = 65f;
            powerUse = 1f;
        }};

        //endregion
        //region upgrades

        dartPad = new MechPad("dart-mech-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 100, Items.graphite, 50, Items.copper, 75));
            mech = Mechs.alpha;
            size = 2;
            consumes[0].power(0.5f);
        }};

        deltaPad = new MechPad("delta-mech-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 175, Items.titanium, 175, Items.copper, 200, Items.silicon, 225, Items.thorium, 150));
            mech = Mechs.delta;
            size = 2;
            consumes[0].power(0.7f);
        }};

        tauPad = new MechPad("tau-mech-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 125, Items.titanium, 125, Items.copper, 125, Items.silicon, 125));
            mech = Mechs.tau;
            size = 2;
            consumes[0].power(1f);
        }};

        omegaPad = new MechPad("omega-mech-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 225, Items.graphite, 275, Items.silicon, 325, Items.thorium, 300, Items.surgealloy, 120));
            mech = Mechs.omega;
            size = 3;
            consumes[0].power(1.2f);
        }};

        javelinPad = new MechPad("javelin-ship-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 175, Items.silicon, 225, Items.titanium, 250, Items.plastanium, 200, Items.phasefabric, 100));
            mech = Mechs.javelin;
            size = 2;
            consumes[0].power(0.8f);
        }};

        tridentPad = new MechPad("trident-ship-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 125, Items.copper, 125, Items.silicon, 125, Items.titanium, 150, Items.plastanium, 100));
            mech = Mechs.trident;
            size = 2;
            consumes[0].power(1f);
        }};

        glaivePad = new MechPad("glaive-ship-pad"){{
            requirements(Category.upgrade, ItemStack.with(Items.lead, 225, Items.silicon, 325, Items.titanium, 350, Items.plastanium, 300, Items.surgealloy, 100));
            mech = Mechs.glaive;
            size = 3;
            consumes[0].power(1.2f);
        }};

        //endregion
        //region sandbox

        powerSource = new PowerSource("power-source"){{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        powerVoid = new PowerVoid("power-void"){{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        itemSource = new ItemSource("item-source"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        itemVoid = new ItemVoid("item-void"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        liquidSource = new LiquidSource("liquid-source"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        liquidVoid = new LiquidVoid("liquid-void"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, ItemStack.with());
            alwaysUnlocked = true;
        }};

        message = new MessageBlock("message"){{
            requirements(Category.effect, ItemStack.with(Items.graphite, 5));
        }};

        illuminator = new LightBlock("illuminator"){{
            requirements(Category.effect, BuildVisibility.lightingOnly, ItemStack.with(Items.graphite, 4, Items.silicon, 2));
            brightness = 0.67f;
            radius = 120f;
            consumes[0].power(0.05f);
        }};

        //endregion
    }
}
