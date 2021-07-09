package test.stateMachine;

/**
 *
 */
public class StateMachineTest {

    public StateMachineTest() {
        test();
    }

    private void test() {
        System.out.println("StateMachine test...........");
        Bob bob = new Bob();
        bob.update();
    }

}
