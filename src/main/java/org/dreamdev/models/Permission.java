package org.dreamdev.models;

public enum Permission {

    CAN_VOTE,
    CAN_UPLOAD_FILE,
    CAN_APPROVE_VOTER,
    CAN_VIEW_METRICS,
    CAN_VIEW_ELECTORATE,
    CAN_VIEW_VOTE,
    CAN_UPDATE_ELECTORATE;

    public static String forState(State state) {
        return "CAN_VOTE_" + state.name();
    }
}
