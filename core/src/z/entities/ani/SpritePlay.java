package z.entities.ani;

import z.entities.type.base.BaseGroundUnit;

/**
 *
 */
public class SpritePlay {

    /**
     *  动画状态记录器
     * */
    public enum AniState {
        IDLE, WALK, ATTACK,
    }


    private BaseGroundUnit unit;

    public AniState aniState = AniState.IDLE;

    public SpritePlay(BaseGroundUnit spriteUnit) {
        this.unit = spriteUnit;
    }

}
