package constants;

public class DiscordIds {

    public static final String NJAL_GUILD_ID;

    static {
        Properties props = Properties.getProps();

        NJAL_GUILD_ID = props.njalGuildId;
        ChannelIds.STANDINGS_REPORT_CHANNEL = props.standingsReportChannel;
        ChannelIds.ROSS_LOG_CHANNEL = props.rossLogChannel;
        ChannelIds.DRAFT_ME_CHANNEL = props.draftMeChannel;
        ChannelIds.REGISTER_CHANNEL = props.registerChannel;
        ChannelIds.PLAYER_LIST_CHANNEL = props.playerListChannel;
        ChannelIds.OVERALL_STANDINGS_CHANNEL = props.overallStandingsChannel;
        CategoryIds.DRAFT_ME_CAT_ID = props.draftMeCatId;
        RoleIds.ADMIN = props.admin;
    }

    public static final class ChannelIds {
        public static String STANDINGS_REPORT_CHANNEL;
        public static String ROSS_LOG_CHANNEL;
        public static String DRAFT_ME_CHANNEL;
        public static String REGISTER_CHANNEL;
        public static String PLAYER_LIST_CHANNEL;
        public static String OVERALL_STANDINGS_CHANNEL;
    }

    public static final class CategoryIds {
        public static String DRAFT_ME_CAT_ID;
    }

    public static final class RoleIds {
        public static String ADMIN;
    }
}