package mindustry.content;

import arc.graphics.Color;
import mindustry.ctype.ContentList;
import mindustry.type.Item;
import mindustry.type.ItemType;

public class Items implements ContentList{
    /** 矿渣*/
    public static Item scrap, /** 铜*/copper, /** 铅*/lead, /**石墨*/graphite, /**煤*/coal, /**钛*/titanium, /**钍*/thorium, /**硅*/silicon, /**塑料*/plastanium, /**相织物*/phasefabric, /**巨浪合金*/surgealloy,
    /**孢子*/sporePod, /**沙*/sand, /**爆炸混合物*/blastCompound, /**硫*/pyratite, /**钢化玻璃*/metaglass,
                // zones add begon
            /**耕地*/farmland, /**小麦*/wheat, /**蔬菜*/vegetables, /**水果*/fruit, /**橄榄*/olives, /**葡萄*/vines, /**肉*/meat,
            /**葡萄酒*/wine, /**油*/oil, /**铁*/iron, /**木材*/timber, /**粘土*/clay, /**大理石*/marble, /**武器*/weapons, /**家具*/furniture, /**瓷器*/pottery, /**鱼*/fish
            //zones add end
                    ;

    @Override
    public void load(){
        // zones add begon  仓库动画顺序
        farmland = new Item("farmland", Color.yellow){{
            alwaysUnlocked = true;
            hardness = 1;
            cost = 1f;
        }};
        wheat = new Item("wheat", Color.yellow, 1){{
            type = ItemType.material;
        }};
        vegetables = new Item("vegetables", Color.yellow, 2){{
            type = ItemType.material;
        }};
        fruit = new Item("fruit", Color.yellow, 3){{
            type = ItemType.material;
        }};
        olives = new Item("olives", Color.yellow, 4){{
            type = ItemType.material;
        }};
        vines = new Item("vines", Color.yellow, 5){{
            type = ItemType.material;
        }};
        meat = new Item("meat", Color.yellow, 6){{
            type = ItemType.material;
        }};
        wine = new Item("wine", Color.yellow, 7){{
            type = ItemType.material;
        }};
        oil = new Item("oil", Color.yellow, 8){{
            type = ItemType.material;
        }};
        iron = new Item("iron", Color.yellow, 9){{
            type = ItemType.material;
        }};
        timber = new Item("timber", Color.yellow, 10){{
            type = ItemType.material;
        }};
        clay = new Item("clay", Color.yellow, 11){{
            type = ItemType.material;
        }};
        marble = new Item("marble", Color.yellow, 12){{
            type = ItemType.material;
        }};
        weapons = new Item("weapons", Color.yellow, 13){{
            type = ItemType.material;
        }};
        furniture = new Item("furniture", Color.yellow, 14){{
            type = ItemType.material;
        }};
        pottery = new Item("pottery", Color.yellow, 15){{
            type = ItemType.material;
        }};
        fish = new Item("fish", Color.yellow, 16){{
            type = ItemType.material;
        }};
        // zones add end

        copper = new Item("copper", Color.valueOf("d99d73")){{
            type = ItemType.material;
            hardness = 1;
            cost = 0.5f;
            alwaysUnlocked = true;
        }};

        lead = new Item("lead", Color.valueOf("8c7fa9")){{
            type = ItemType.material;
            hardness = 1;
            cost = 0.7f;
        }};

        metaglass = new Item("metaglass", Color.valueOf("ebeef5")){{
            type = ItemType.material;
            cost = 1.5f;
        }};

        graphite = new Item("graphite", Color.valueOf("b2c6d2")){{
            type = ItemType.material;
            cost = 1f;
        }};

        sand = new Item("sand", Color.valueOf("f7cba4")){{

        }};

        coal = new Item("coal", Color.valueOf("272727")){{
            explosiveness = 0.2f;
            flammability = 1f;
            hardness = 2;
        }};

        titanium = new Item("titanium", Color.valueOf("8da1e3")){{
            type = ItemType.material;
            hardness = 3;
            cost = 1f;
        }};

        thorium = new Item("thorium", Color.valueOf("f9a3c7")){{
            type = ItemType.material;
            explosiveness = 0.2f;
            hardness = 4;
            radioactivity = 1f;
            cost = 1.1f;
        }};

        scrap = new Item("scrap", Color.valueOf("777777")){{

        }};

        silicon = new Item("silicon", Color.valueOf("53565c")){{
            type = ItemType.material;
            cost = 0.8f;
        }};

        plastanium = new Item("plastanium", Color.valueOf("cbd97f")){{
            type = ItemType.material;
            flammability = 0.1f;
            explosiveness = 0.2f;
            cost = 1.3f;
        }};

        phasefabric = new Item("phase-fabric", Color.valueOf("f4ba6e")){{
            type = ItemType.material;
            cost = 1.3f;
            radioactivity = 0.6f;
        }};

        surgealloy = new Item("surge-alloy", Color.valueOf("f3e979")){{
            type = ItemType.material;
        }};

        sporePod = new Item("spore-pod", Color.valueOf("7457ce")){{
            flammability = 1.15f;
        }};

        blastCompound = new Item("blast-compound", Color.valueOf("ff795e")){{
            flammability = 0.4f;
            explosiveness = 1.2f;
        }};

        pyratite = new Item("pyratite", Color.valueOf("ffaa5f")){{
            flammability = 1.4f;
            explosiveness = 0.4f;
        }};
    }
}
