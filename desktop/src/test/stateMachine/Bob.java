package test.stateMachine;

import arc.ai.fsm.DefaultStateMachine;
import arc.ai.fsm.StateMachine;
import arc.ai.msg.Telegram;
import arc.ai.msg.Telegraph;

/**
 *
 */
/** @author davebaol */
public class Bob implements Telegraph {
    // the amount of gold a miner must have before he feels comfortable
    final public static int COMFORT_LEVEL = 5;
    // the amount of nuggets a miner can carry
    final public static int MAX_NUGGETS = 3;
    // above this value a miner is thirsty
    final public static int THIRST_LEVEL = 5;
    // above this value a miner is sleepy
    final public static int TIREDNESS_THRESHOLD = 5;

    private StateMachine<Bob, BobState> stateMachine;

    public Bob () {
        stateMachine = new DefaultStateMachine<Bob, BobState>(this);
        stateMachine.setInitialState(new BobState());
        stateMachine.changeState(new BobState());
    }


    @Override
    public boolean handleMessage (Telegram msg) {
        return stateMachine.handleMessage(msg);
    }

    public void update () {
        stateMachine.update();
    }

    public StateMachine<Bob, BobState> getStateMachine () {
        return stateMachine;
    }

}
