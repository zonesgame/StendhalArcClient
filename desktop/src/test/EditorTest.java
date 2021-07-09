package test;

import arc.Core;
import arc.graphics.g2d.TextureAtlas;

/**
 *
 */
public class EditorTest {

    public EditorTest() {
        test();
    }

    private void test() {
        TextureAtlas atlas = new TextureAtlas(Core.files.absolute("D:\\Develop\\workspace\\libgdx\\zones\\Public\\DiabloTown\\SanGuoTD\\core\\assets\\sprites\\sprites.atlas"));
        System.out.println(atlas.getTextures().size);
    }

}
