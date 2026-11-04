@Library('wf-ci') _

def libs = 'versions/1.12.2/build/libs versions/1.20.1/fabric/build/libs versions/1.20.1/forge/build/libs ' +
    'versions/1.21.1/fabric/build/libs versions/1.21.1/neoforge/build/libs versions/1.21.1/lexforge/build/libs'

wfBuild(
    repo: 'Ultimate-Weight',
    jdk: '25',
    branches: ['master', 'main'],
    build: '--no-daemon -PmodVersion=1.0.0 :1.12.2:build :1.20.1:fabric:build :1.20.1:forge:build :1.21.1:fabric:build ' +
        ':1.21.1:neoforge:build :1.21.1:neoforge:shadowJar :1.21.1:lexforge:build :1.21.1:lexforge:shadowJar',
    publish: '--no-daemon -PmodVersion=1.0.0 :1.21.1:neoforge:publish',
    archive: 'versions/**/build/libs/*.jar',
    latest: [
        tag: 'snapshots',
        title: 'Snapshots',
        notes: 'Rolling snapshot build for Minecraft 1.12.2, 1.20.1 and 1.21.1 from commit ${sha}.',
        filesCmd: "find ${libs} -maxdepth 1 -name '*.jar' ! -name '*-dev.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | sort",
        prerelease: true,
    ],
    tagRolling: [
        tag: 'wf', jdk: '25', build: ':1.20.1:forge:build -PmodVersion=1.2.0 --no-daemon',
        stage: '''
src=$(find versions/1.20.1/forge/build/libs -maxdepth 1 -name 'wfweight-1.20.1-Forge-*.jar' ! -name '*-sources*' ! -name '*-dev*' ! -name '*-slim*' | sort | head -1)
[ -n "$src" ] || { echo "No release jar found in versions/1.20.1/forge/build/libs" >&2; exit 1; }
unzip -l "$src" | grep -q 'ultimateweight/wfweight/shadow/org/yaml/snakeyaml/Yaml.class' \\
  || { echo "Staged jar $src is missing shaded SnakeYAML (thin jar). Refusing to publish." >&2; exit 1; }
cp "$src" wfweight.jar
''',
        filesCmd: 'echo wfweight.jar',
    ],
)
