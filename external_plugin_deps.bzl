load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  maven_jar(
    name = 'redmineapi',
    artifact = 'com.taskadapter:redmine-java-api:3.0.1',
    sha1 = 'd315913ccb7d4ee7bcb853a0c8f11c442e30e331',
  )
