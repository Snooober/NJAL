# NJAL
Discord bot used to organize NJAL (Nicely Jobbed Artifact League) tournaments.

To use the bot, import the "njal_nodata.sql" tables into an sql database.

Create a file "config.properties" in src/main/resources with the following properties:

    DB_URL (database url)
    user (database username)
    pass (database password)
    token (discord bot token)
    guildId (discord guild ID for your server)
    standingsReportChannel (discord channel ID for the channel you would like the round-by-round standings to be sent)
    logChannel (discord channel ID for logging events to admins)
    draftMeChannel (discord channel ID for using Draft Me! system)
    registerChannel (discord channel ID for registration channel)
    playerListChannel (discord channel ID for a constantly updated table of registered players)
    overallStandingsChannel (discord channel ID for a constantly updated table of overall standings)
    adminCommandsChannel (discord channel ID for input of admin commands)
    draftMeCatId (discord category ID for Draft Me! system)
    adminRole (discord role ID for admins)

A discord role must also be created named "Registered", and the bot must be given sufficient permissions.
