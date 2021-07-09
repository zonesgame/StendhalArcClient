package test.stateMachine;

import arc.ai.msg.Telegram;

/**
 *
 */
public class BobState implements LSDState<Bob> {


    @Override
    public void enter(Bob entity) {
        System.out.println("enter  state..");
    }

    @Override
    public void update(Bob entity) {
        System.out.println("state update.......");
    }

    @Override
    public void exit(Bob entity) {
        System.out.println("exit state");
    }

    @Override
    public boolean onMessage(Bob entity, Telegram telegram) {
        return false;
    }
}
