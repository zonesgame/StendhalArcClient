package test;

import arc.ApplicationCore;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import mindustry.Vars;
import test.stateMachine.StateMachineTest;

public class TestLauncher extends ApplicationCore {

    public static void main(String[] arg){
        try{
            Vars.loadLogger();
            new SdlApplication(new TestLauncher(arg), new SdlConfig(){{
                title = "Mindustry";
//                maximized = true;
                depth = 0;
                stencil = 0;
                width = 1280;
                height = 720;
            }});
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    public TestLauncher(String[] args){
    }


    @Override
    public void setup() {
//        new JsonTest();
//        new EditorTest();
        new StateMachineTest();
    }

}
