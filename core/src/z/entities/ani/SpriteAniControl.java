package z.entities.ani;

import arc.graphics.g2d.Animation;
import arc.math.Mathf;
import arc.util.Time;

/**
 *
 */
public class SpriteAniControl {
    private static final SpriteAniControl instance = new SpriteAniControl();

    public static SpriteAniControl get() {
        return instance;
    }

//    private final int DIRECTION = 8;
//    private AniPlay aniPlay = new AniPlay();
//    private AniData aniData;
    private Animation.PlayMode playMode = Animation.PlayMode.LOOP;
    private int framesLength;
    private float frameDuration;
    private float animationDuration;
    private int lastFrameNumber;
    private float lastStateTime;
    private int curFrame;

    private float stateTime = 0;
    private int mode = 0;

    public SpriteAniControl() {
//        this.aniData = data;
        this.frameDuration = 6f;
    }

    public void setMode(byte mode) {
        if (this.mode != mode) {
            this.mode = mode;
            stateTime = 0;
        }
    }

    public SpriteAniControl setFrameDuration(float value) {
        this.frameDuration = value;
        return this;
    }

    public SpriteAniControl setFrameData(int frameLength, float frameDuration) {
        this.framesLength = frameLength;
        this.frameDuration = frameDuration;
        return this;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void update() {
        stateTime += Time.delta();
    }

    /**
     * @param offset 绘制偏移量
     * */
//    public TextureRegion getRegion(float angle, Vec2 offset) {
//        float dirAngle = round / aniData.getDirCount(mode);
//        float newAngle = (angle + dirAngle / 2);
//        newAngle = newAngle % 360;
//        int dirIndex = (int) (newAngle / dirAngle);
//        framesLength = aniData.getFrameCount(mode, dirIndex);
//        curFrame = getKeyFrameIndex(stateTime);
//
//        return aniData.getRegionOrOffset(mode, dirIndex, curFrame, offset);
////        return aniData.getRegion(mode, dirIndex, curFrame);
//    }

    /**
     * Returns a frame based on the so called state time. This is the amount of seconds an object has spent in the
     * state this Animation instance represents, e.g. running, jumping and so on. The mode specifies whether the animation is
     * looping or not.
     * @param stateTime the time spent in the state represented by this animation.
     * @param looping whether the animation is looping or not.
     * @return the frame of animation for the given state time.
     */
    public int getKeyFrameIndex(float stateTime, int frameCount, boolean looping){
        this.framesLength = frameCount;
        // we set the play mode by overriding the previous mode based on looping
        // parameter value
        Animation.PlayMode oldPlayMode = playMode;
        if(looping && (playMode == Animation.PlayMode.NORMAL || playMode == Animation.PlayMode.REVERSED)){
            if(playMode == Animation.PlayMode.NORMAL)
                playMode = Animation.PlayMode.LOOP;
            else
                playMode = Animation.PlayMode.LOOP_REVERSED;
        }else if(!looping && !(playMode == Animation.PlayMode.NORMAL || playMode == Animation.PlayMode.REVERSED)){
            if(playMode == Animation.PlayMode.LOOP_REVERSED)
                playMode = Animation.PlayMode.REVERSED;
            else
                playMode = Animation.PlayMode.LOOP;
        }

        int frame = getKeyFrameIndex(stateTime);
        playMode = oldPlayMode;
        return frame;
    }

    public int getKeyFrameIndex(float stateTime, boolean looping){
        return getKeyFrameIndex(stateTime, framesLength, looping);
    }

    /**
     * Returns the current frame number.
     * @return current frame number
     */
    public int getKeyFrameIndex(float stateTime){
        if(framesLength == 1) return 0;

        int frameNumber = (int)(stateTime / frameDuration);
        switch(playMode){
            case NORMAL:
                frameNumber = Math.min(framesLength - 1, frameNumber);
                break;
            case LOOP:
                frameNumber = frameNumber % framesLength;
                break;
            case LOOP_PINGPONG:
                frameNumber = frameNumber % ((framesLength * 2) - 2);
                if(frameNumber >= framesLength)
                    frameNumber = framesLength - 2 - (frameNumber - framesLength);
                break;
            case LOOP_RANDOM:
                int lastFrameNumber = (int)((lastStateTime) / frameDuration);
                if(lastFrameNumber != frameNumber){
                    frameNumber = Mathf.random(framesLength - 1);
                }else{
                    frameNumber = this.lastFrameNumber;
                }
                break;
            case REVERSED:
                frameNumber = Math.max(framesLength - frameNumber - 1, 0);
                break;
            case LOOP_REVERSED:
                frameNumber = frameNumber % framesLength;
                frameNumber = framesLength - frameNumber - 1;
                break;
        }

        lastFrameNumber = frameNumber;
        lastStateTime = stateTime;

        return frameNumber;
    }

    // zones add function begon

    public int getLoopFrame() {
        return (int)(stateTime / frameDuration) % framesLength;
    }

    public int getTimeFrame() {
        return (int)(Time.time() / frameDuration) % framesLength;
    }

    // zones add function end

    private static final int[] directionIndex = {2, 1, 0, 7, 6, 5, 4, 3};

    private static final float dirAngle = 360 / 8f;
    private static final float round = 360f;
    /** 获取权倾天下方向(caesar)*/
    public static int getFrameDir(float rotation) {
//        private final float round = 360f;
//        private final int dirCount_8 = 8;
//        float dirAngle = round / dirCount_8;
//        float newAngle = (rotation + dirAngle / 2);
//        newAngle = newAngle % 360;
//        int dirIndex = (int) (newAngle / dirAngle);

        // 方案2
//        float newAngle = (rotation + dirAngle / 2) % round;
//        return (int) (newAngle / dirAngle);

        return (int) (((rotation + dirAngle / 2) % round) / dirAngle);
    }

    /** qqtx*/
    public static int getFrameDirQQTX(float rotation) {
        return directionIndex[getFrameDir(rotation)];
    }

    /** 获取当前帧通过动画百分比*/
    public static int getFrameIndexFromScale(int framesLength, boolean looping, float frameScale){
        if(framesLength == 1) return 0;

        int frameNumber = (int)(frameScale * framesLength);
        if (looping) {
            frameNumber = frameNumber % framesLength;
        }
        else {
            frameNumber = Math.min(framesLength - 1, frameNumber);
        }

        return frameNumber;
    }

    public static int getFrameIndexFromScale(int framesLength, float frameScale){
        return getFrameIndexFromScale(framesLength, false, frameScale);
    }

}
