package mindustry.content;

import java.lang.reflect.Field;

import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Array;
import arc.tools.file.xml.PackXmlCreate;
import arc.util.serialization.XmlReader.Element;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import z.debug.ZDebug;

import static mindustry.content.Blocks.*;
import static z.debug.ZDebug.create_TechTreeXmlFile;
import static z.debug.ZDebug.enable_xmlTechTree;

/**
 *  科技树
 * */
public class TechTree implements ContentList{
    /** 所有科技节点*/
    public static Array<TechNode> all;
    /** 起始节点*/
    public static TechNode root;

    @Override
    public void load(){
        TechNode.context = null;
        all = new Array<>();

        if ( enable_xmlTechTree && !create_TechTreeXmlFile) {
            Element techRoot = Vars.xmlReader.parse(Core.files.absolute(ZDebug.techTreeFile));
            root = readTechNode(techRoot);
            return;
        }

        root = node(coreShard, () -> {

            node(conveyor, () -> {

                node(junction, () -> {
                    node(router, () -> {
                        node(launchPad, () -> {
                            node(launchPadLarge, () -> {

                            });
                        });

                        node(distributor);
                        node(sorter, () -> {
                            node(invertedSorter);
                            node(message);
                            node(overflowGate, () -> {
                                node(underflowGate);
                            });
                        });
                        node(container, () -> {
                            node(unloader);
                            node(vault, () -> {

                            });
                        });

                        node(itemBridge, () -> {
                            node(titaniumConveyor, () -> {
                                node(phaseConveyor, () -> {
                                    node(massDriver, () -> {

                                    });
                                });

                                node(armoredConveyor, () -> {

                                });
                            });
                        });
                    });
                });
            });

            node(duo, () -> {
                node(scatter, () -> {
                    node(hail, () -> {

                        node(salvo, () -> {
                            node(swarmer, () -> {
                                node(cyclone, () -> {
                                    node(spectre, () -> {

                                    });
                                });
                            });

                            node(ripple, () -> {
                                node(fuse, () -> {

                                });
                            });
                        });
                    });
                });

                node(scorch, () -> {
                    node(arc, () -> {
                        node(wave, () -> {

                        });

                        node(lancer, () -> {
                            node(meltdown, () -> {

                            });

                            node(shockMine, () -> {

                            });
                        });
                    });
                });


                node(copperWall, () -> {
                    node(copperWallLarge, () -> {
                        node(titaniumWall, () -> {
                            node(titaniumWallLarge);

                            node(door, () -> {
                                node(doorLarge);
                            });
                            node(plastaniumWall, () -> {
                                node(plastaniumWallLarge, () -> {

                                });
                            });
                            node(thoriumWall, () -> {
                                node(thoriumWallLarge);
                                node(surgeWall, () -> {
                                    node(surgeWallLarge);
                                    node(phaseWall, () -> {
                                        node(phaseWallLarge);
                                    });
                                });
                            });
                        });
                    });
                });
            });

            node(mechanicalDrill, () -> {
                node(graphitePress, () -> {
                    node(pneumaticDrill, () -> {
                        node(cultivator, () -> {

                        });

                        node(laserDrill, () -> {
                            node(blastDrill, () -> {

                            });

                            node(waterExtractor, () -> {
                                node(oilExtractor, () -> {

                                });
                            });
                        });
                    });

                    node(pyratiteMixer, () -> {
                        node(blastMixer, () -> {

                        });
                    });

                    node(siliconSmelter, () -> {

                        node(sporePress, () -> {
                            node(coalCentrifuge, () -> {

                            });
                            node(multiPress, () -> {

                            });

                            node(plastaniumCompressor, () -> {
                                node(phaseWeaver, () -> {

                                });
                            });
                        });

                        node(kiln, () -> {
                            node(incinerator, () -> {
                                node(melter, () -> {
                                    node(surgeSmelter, () -> {

                                    });

                                    node(separator, () -> {
                                        node(pulverizer, () -> {

                                        });
                                    });

                                    node(cryofluidMixer, () -> {

                                    });
                                });
                            });
                        });
                    });
                });


                node(mechanicalPump, () -> {
                    node(conduit, () -> {
                        node(liquidJunction, () -> {
                            node(liquidRouter, () -> {
                                node(liquidTank);

                                node(bridgeConduit);

                                node(pulseConduit, () -> {
                                    node(phaseConduit, () -> {

                                    });

                                    node(platedConduit, () -> {

                                    });
                                });

                                node(rotaryPump, () -> {
                                    node(thermalPump, () -> {

                                    });
                                });
                            });
                        });
                    });
                });

                node(combustionGenerator, () -> {
                    node(powerNode, () -> {
                        node(powerNodeLarge, () -> {
                            node(diode, () -> {
                                node(surgeTower, () -> {

                                });
                            });
                        });

                        node(battery, () -> {
                            node(batteryLarge, () -> {

                            });
                        });

                        node(mender, () -> {
                            node(mendProjector, () -> {
                                node(forceProjector, () -> {
                                    node(overdriveProjector, () -> {

                                    });
                                });

                                node(repairPoint, () -> {

                                });
                            });
                        });

                        node(turbineGenerator, () -> {
                            node(thermalGenerator, () -> {
                                node(differentialGenerator, () -> {
                                    node(thoriumReactor, () -> {
                                        node(impactReactor, () -> {

                                        });

                                        node(rtgGenerator, () -> {

                                        });
                                    });
                                });
                            });
                        });

                        node(solarPanel, () -> {
                            node(largeSolarPanel, () -> {

                            });
                        });
                    });

                    node(draugFactory, () -> {
                        node(spiritFactory, () -> {
                            node(phantomFactory);
                        });

                        node(daggerFactory, () -> {
                            node(commandCenter, () -> {});
                            node(crawlerFactory, () -> {
                                node(titanFactory, () -> {
                                    node(fortressFactory, () -> {

                                    });
                                });
                            });

                            node(wraithFactory, () -> {
                                node(ghoulFactory, () -> {
                                    node(revenantFactory, () -> {

                                    });
                                });
                            });
                        });
                    });

                    node(dartPad, () -> {
                        node(deltaPad, () -> {

                            node(javelinPad, () -> {
                                node(tridentPad, () -> {
                                    node(glaivePad);
                                });
                            });

                            node(tauPad, () -> {
                                node(omegaPad, () -> {

                                });
                            });
                        });
                    });
                });
            });
        });

        if (create_TechTreeXmlFile) {   // 生成科技树xml配置文件
            Fi createFile = Core.files.absolute("D:\\Develop\\workspace\\libgdx\\zones\\Public\\DiabloTown\\SanGuoTD\\core\\assets-raw\\zonesAdd\\createFile\\techTree.xml");
            PackXmlCreate xmlCreate = PackXmlCreate.get().create(createFile);
            xmlCreate.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            // <?xml version="1.0" encoding="UTF-8"?>
            writeNode(root, xmlCreate);
            xmlCreate.close();
        }
    }

    /** xml文件创建使用*/
    private void writeNode(TechNode root, PackXmlCreate xmlCreate) {
        String elementName = getBlockFieldName(root.block);
        {
            xmlCreate.addElement("node");
            xmlCreate.addAttribute("block", elementName);
        }

        for (TechNode node : root.children) {
            writeNode(node, xmlCreate);
        }

        xmlCreate.pop();
    }

    /** xml文件创建使用*/
    private String getBlockFieldName(Block block) {
        try {
//            Blocks blocks = new Blocks();
            Field[] fields = Blocks.class.getFields();
            for (Field field : fields) {
                if (field.get(null) == block) {
                    return field.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** xml配置文件读取科技节点*/
    private TechNode readTechNode(Element root) {
        TechNode returnValue;
        if (root.getChildCount() > 0) {
            returnValue = node(Vars.xmlSerialize.getBlock(root.getAttribute("block")), () -> {
                for (int i = 0; i < root.getChildCount(); i++) {
                    readTechNode(root.getChild(i));
                }
            });
        } else {
            returnValue = node(Vars.xmlSerialize.getBlock(root.getAttribute("block")));
        }

        return returnValue;
    }

    /** 创建科技节点
     * @param block 解锁科技块
     * @param children 包含的子节点列表
     * */
    private static TechNode node(Block block, Runnable children){
        ItemStack[] requirements = new ItemStack[block.requirements.length];
        for(int i = 0; i < requirements.length; i++){
            requirements[i] = new ItemStack(block.requirements[i].item, 40 + Mathf.round(Mathf.pow(block.requirements[i].amount, 1.25f) * 6, 10));
        }

        return new TechNode(block, requirements, children);
    }

    /** 创建节点*/
    private static TechNode node(Block block){
        return node(block, () -> {});
    }

    /** 创建节点*/
    public static TechNode create(Block parent, Block block){
        TechNode.context = all.find(t -> t.block == parent);
        return node(block, () -> {});
    }

    /**
     *  科技节点
     * */
    public static class TechNode{
        /** 节点内容*/
        static TechNode context;

        /** 绑定科技块*/
        public final Block block;
        /** 科技需求内容*/
        public final ItemStack[] requirements;
        /** 子科技节点容器*/
        public final Array<TechNode> children = new Array<>();

        TechNode(TechNode ccontext, Block block, ItemStack[] requirements, Runnable children){
            if(ccontext != null){
                ccontext.children.add(this);
            }

            this.block = block;
            this.requirements = requirements;

            context = this;
            children.run();
            context = ccontext;
            all.add(this);
        }

        TechNode(Block block, ItemStack[] requirements, Runnable children){
            this(context, block, requirements, children);
        }
    }
}
