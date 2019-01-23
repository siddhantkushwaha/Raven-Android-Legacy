package com.siddhantkushwaha.raven.utility;

public class FirebasePath {

    private static String root_branch = "users";
    private static String thread_branch = "all_threads";
    private static String user_list_branch = "all_users";
    private static String profile_branch = "user_profile";
    private static String threads_root = "threads";
    private static String thread_details = "thread_details";
    private static String thread_messages = "message_list";

    public static String userRoot() {
        return root_branch + "/";
    }

    public static String pathToProfile(String uid) {
        return root_branch + "/" + uid + "/" + profile_branch;
    }

    public static String pathToUserList(String uid) {
        return root_branch + "/" + uid + "/" + user_list_branch;
    }

    public static String pathToThreadsList(String uid) {
        return root_branch + "/" + uid + "/" + thread_branch;
    }

    public static String pathToOnline(String uid) {
        return "userPresence" + "/" + uid;
    }

    public static String pathToThreadsRoot() {
        return threads_root;
    }

    public static String pathToThreadDetails(String thread_key) {
        return threads_root + "/" + thread_key + "/" + thread_details;
    }

    public static String pathToThreadMessageList(String thread_key) {
        return threads_root + "/" + thread_key + "/" + thread_messages;
    }
}
