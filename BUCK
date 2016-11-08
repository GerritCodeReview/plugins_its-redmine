include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'its-redmine',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: its-redmine',
    'Gerrit-ReloadMode: reload',
    'Gerrit-ApiVersion: 2.13',
    'Gerrit-Module: com.googlesource.gerrit.plugins.its.redmine.RedmineModule',
    'Implementation-Title: Redmine ITS Plugin',
    'Implementation-URL: http://www.savoirfairelinux.com',
    'Implementation-Vendor: Savoir-faire Linux Inc',
  ],
  deps = [
    ':its-base_stripped',
    '//plugins/its-redmine/lib/gerrit:redmine-api',
    '//plugins/its-redmine/lib/gerrit:json-api',
  ],
)

def strip_jar(
  name,
  src,
  excludes = [],
  visibility = [],
):
  name_zip = name + '.zip'
  genrule(
    name = name_zip,
    cmd = 'cp -f $SRCS $OUT && zip -qd $OUT ' + ' '.join(excludes),
    srcs = [ src ],
    out = name_zip,
    visibility = visibility,
  )
  prebuilt_jar(
    name = name,
    binary_jar = ':' + name_zip,
    visibility = visibility,
  )

strip_jar(
  name = 'its-base_stripped',
  src = '//plugins/its-base:its-base',
  excludes = [
    'Documentation/*',
  ]
)


