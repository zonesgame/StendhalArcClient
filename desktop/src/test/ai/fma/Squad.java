//package test.ai.fma;
//
//import arc.ai.fma.Formation;
//import arc.ai.fma.FormationMotionModerator;
//import arc.ai.fma.FormationPattern;
//import arc.math.geom.Vec2;
//import arc.struct.Array;
//import z.ai.utils.Location2;
//
///**
// *
// */
//public class Squad<T> {
//
//    public enum FormationPatternType {
//        Line, Column, Square, Ring, V, Wedge;
//    }
//
//    public static final float PATTERN_SPACING = Constants.unitRadius * 0.25f;
//    public static final FormationPatternType DEFAULT_PATTERN = FormationPatternType.V;
//
//
//    // Array for formation API and ashley entity reference
//    public Array<T> members = new Array<T>();
//
//    // Formation
//    public Formation<Vec2> formation;
//    public FormationMotionModerator<Vec2> moderator;
//    public Location2 targetLocation = new Location2();
//
//
//    /** Can only be created by PooledEngine */
//    public Squad () {
//        // private constructor
//    }
//
//
//    public void addMember (Entity entity) {
//        members.add(entity);
//        memberAgents.add(Components.STEERABLE.get(entity));
//        formation.addMember(Components.UNIT.get(entity));
//    }
//
//    public void removeMember (Entity entity) {
//        members.removeValue(entity, true);
//        memberAgents.removeValue(Components.STEERABLE.get(entity), true);
//        formation.removeMember(Components.UNIT.get(entity));
//    }
//
//    public void track (Entity self, Entity target) {
//        if ((target.flags & EntityCategory.RESOURCE) == EntityCategory.RESOURCE) {
//            resourcesTracked.add(target);
//            resourceAgents.add(Components.STEERABLE.get(target));
//            sortTrackedResources();
//            MessageManager.getInstance().dispatchMessage(null, Components.FSM.get(self),
//                    TelegramMessage.DISCOVERED_RESOURCE.ordinal());
//        } else if ((target.flags & EntityCategory.SQUAD) == EntityCategory.SQUAD) {
//            if (EntityUtils.isSameFaction(self, target)) {
//                friendliesTracked.add(target);
//                friendlyAgents.add(Components.STEERABLE.get(target));
//            } else {
//                enemiesTracked.add(target);
//                MessageManager.getInstance().dispatchMessage(null, Components.FSM.get(self), TelegramMessage.DISCOVERED_ENEMY.ordinal());
//            }
//
//        }
//    }
//
//    public void untrack (Entity self, Entity target) {
//        if ((target.flags & EntityCategory.RESOURCE) == EntityCategory.RESOURCE) {
//            resourcesTracked.removeValue(target, true);
//            resourceAgents.removeValue(Components.STEERABLE.get(target), true);
//        } else if ((target.flags & EntityCategory.SQUAD) == EntityCategory.SQUAD) {
//            if (EntityUtils.isSameFaction(self, target)) {
//                friendliesTracked.removeValue(target, true);
//                friendlyAgents.removeValue(Components.STEERABLE.get(target), true);
//            }
//            enemiesTracked.removeValue(target, true);
//        }
//    }
//
//    private Vector2 getPosition () {
//        return steerable.getPosition();
//    }
//
//    private void sortTrackedResources () {
//        resourcesTracked.sort(resourceComparator);
//    }
//
//    public void setFormationPattern (FormationPatternType pattern) {
//        FormationPattern<Vector2> formationPattern = getFormationPattern(pattern);
//        formation.changePattern(formationPattern);
//    }
//
//    public FormationPattern<Vec2> getFormationPattern (FormationPatternType pattern) {
//        switch (pattern) {
//            case Line:
//                return new LineFormationPattern(Constants.unitRadius + PATTERN_SPACING);
//            case Column:
//                return new ColumnFormationPattern(Constants.unitRadius + PATTERN_SPACING);
//            case Square:
//                return new SquareFormationPattern(Constants.unitRadius + PATTERN_SPACING);
//            default:
//            case Ring:
//                return new OffensiveCircleFormationPattern<Vector2>(Constants.unitRadius + PATTERN_SPACING);
//            case V:
//                return new VFormationPattern(60 * MathUtils.degreesToRadians, Constants.unitRadius + PATTERN_SPACING);
//            case Wedge:
//                return new WedgeFormationPattern(Constants.unitRadius + PATTERN_SPACING);
//        }
//    }
//
//    public void setTarget (Vec2 target) {
//        targetLocation.getPosition().set(target);
//    }
//
//    @Override
//    public void reset () {
//        members.clear();
//        memberAgents.clear();
//    }
//
//}
//
