load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "its-redmine",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: its-redmine",
        "Gerrit-Module: com.googlesource.gerrit.plugins.its.redmine.RedmineModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.its.redmine.InitRedmine",
        "Gerrit-ReloadMode: reload",
        "Implementation-Title: Redmine ITS Plugin",
    ],
    deps = [
        "//plugins/its-base",
        "@redmineapi//jar",
    ],
)

junit_tests(
    name = "its_redmine_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-redmine"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
         "@redmineapi//jar",
        ":its-redmine__plugin",
        "//plugins/its-base:its-base",
        "//plugins/its-base:its-base_tests-utils",
    ],
)
