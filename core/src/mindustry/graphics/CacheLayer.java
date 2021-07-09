package mindustry.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.Shader;

import static arc.Core.camera;
import static mindustry.Vars.renderer;

/**
 *  缓存图层
 * */
public enum CacheLayer{
    /** 水源图层*/
    water{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.water);
        }
    },
    /** 焦油图层*/
    tar{
        @Override
        public void begin(){
            beginShader();
        }

        @Override
        public void end(){
            endShader(Shaders.tar);
        }
    },
    /** 正常图层*/
    normal,
    /** 墙体图层*/
    walls;

    public void begin(){

    }

    public void end(){

    }

    void beginShader(){
        if(!Core.settings.getBool("animatedwater")) return;

        renderer.blocks.floor.endc();
        renderer.shieldBuffer.begin();
        Core.graphics.clear(Color.clear);
        renderer.blocks.floor.beginc();
    }

    void endShader(Shader shader){
        if(!Core.settings.getBool("animatedwater")) return;

        renderer.blocks.floor.endc();
        renderer.shieldBuffer.end();

        Draw.shader(shader);
        Draw.rect(Draw.wrap(renderer.shieldBuffer.getTexture()), camera.position.x, camera.position.y, camera.width, -camera.height);
        Draw.shader();

        renderer.blocks.floor.beginc();
    }
}
