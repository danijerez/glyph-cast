import { createHash } from 'node:crypto'
import { execFileSync } from 'node:child_process'
import { existsSync, mkdirSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs'
import { join, delimiter } from 'node:path'
import { homedir } from 'node:os'
import { fileURLToPath } from 'node:url'

const ROOT = fileURLToPath(new URL('.', import.meta.url))
const SDK = process.env.GLYPHCAST_TOOLS ?? join(process.env.LOCALAPPDATA ?? homedir(), 'recreo-android')
const BUILD = join(ROOT, 'build')
const OUT = join(ROOT, 'bin', 'glyphcast.apk')
const REPOSITORY = 'https://dl.google.com/android/repository/'
const BUILD_TOOLS = {
  win32: { file: 'build-tools_r34-windows.zip', sha1: '62cfde1b6fcc3ad12a4d2ba1b537e752768bfd47' },
  darwin: { file: 'build-tools_r34-macosx.zip', sha1: '00e1301387028f33753e38274401694e1a27c62f' },
  linux: { file: 'build-tools_r34-linux.zip', sha1: 'd6d58e0c6925a9e4d9a541e84cd1f405c2f9d2a9' }
}
const PACKAGES = [
  BUILD_TOOLS[process.platform] ?? BUILD_TOOLS.linux,
  { file: 'platform-34-ext7_r03.zip', sha1: '1f2e9478d6a7601425ceaa553311dc43191f103d' }
]
const EXE = process.platform === 'win32' ? '.exe' : ''
const GLYPH_SDK = { url: 'https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit/raw/main/glyph-matrix-sdk-2.0.aar', file: join(ROOT, 'vendor', 'glyph-matrix-sdk-2.0.aar'), sha1: '39206df6638a07382af0179e71a8a80c99fcf11c' }
const R8 = { url: 'https://dl.google.com/dl/android/maven2/com/android/tools/r8/8.13.25/r8-8.13.25.jar', file: join(SDK, 'r8-8.13.25.jar') }
const MIN_SDK = 34
const TARGET_SDK = 34
const VERSION = { code: '17', name: '2.4.0' }
const KEY = {
  file: process.env.GLYPHCAST_KEYSTORE ?? join(SDK, 'glyph.keystore'),
  alias: process.env.GLYPHCAST_KEY_ALIAS ?? 'glyph',
  password: process.env.GLYPHCAST_KEY_PASS ?? 'glyph-toys'
}
const TAR = process.env.SystemRoot ? join(process.env.SystemRoot, 'System32', 'tar.exe') : 'tar'

const run = (command, args) => execFileSync(command, args, { stdio: 'inherit' })

const extract = (archive, target) => process.platform === 'linux' ? run('unzip', ['-q', '-o', archive, '-d', target]) : run(TAR, ['-xf', archive, '-C', target])

const find = (dir, name) => {
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry)
    if (entry === name) return path
    if (statSync(path).isDirectory()) {
      const found = find(path, name)
      if (found) return found
    }
  }
  return null
}

const files = dir => readdirSync(dir).flatMap(entry => {
  const path = join(dir, entry)
  return statSync(path).isDirectory() ? files(path) : [path]
})

const download = async url => {
  const response = await fetch(url)
  if (!response.ok) throw new Error(`Download failed (${response.status}): ${url}`)
  return Buffer.from(await response.arrayBuffer())
}

const sha1 = data => createHash('sha1').update(data).digest('hex')

mkdirSync(SDK, { recursive: true })
for (const { file, sha1: expected } of PACKAGES) {
  const zip = join(SDK, file)
  const target = zip.replace(/\.zip$/, '')
  if (existsSync(join(target, '.done'))) continue
  console.log(`Downloading ${file}`)
  const data = await download(REPOSITORY + file)
  if (sha1(data) !== expected) throw new Error(`Checksum mismatch for ${file}`)
  writeFileSync(zip, data)
  rmSync(target, { recursive: true, force: true })
  mkdirSync(target, { recursive: true })
  extract(zip, target)
  writeFileSync(join(target, '.done'), '')
  rmSync(zip)
}

if (!existsSync(R8.file)) {
  console.log('Downloading D8')
  const data = await download(R8.url)
  if (sha1(data) !== (await download(R8.url + '.sha1')).toString().trim()) throw new Error('Checksum mismatch for D8')
  writeFileSync(R8.file, data)
}

if (!existsSync(GLYPH_SDK.file)) {
  console.log('Downloading Glyph Matrix SDK')
  mkdirSync(join(ROOT, 'vendor'), { recursive: true })
  const data = await download(GLYPH_SDK.url)
  if (sha1(data) !== GLYPH_SDK.sha1) throw new Error('Checksum mismatch for the Glyph Matrix SDK')
  writeFileSync(GLYPH_SDK.file, data)
}

const [buildTools, platform] = PACKAGES.map(({ file }) => join(SDK, file.replace(/\.zip$/, '')))
const aapt2 = find(buildTools, 'aapt2' + EXE)
const zipalign = find(buildTools, 'zipalign' + EXE)
const apksigner = find(buildTools, 'apksigner.jar')
const androidJar = find(platform, 'android.jar')

rmSync(BUILD, { recursive: true, force: true })
for (const dir of ['aar', 'gen', 'classes', 'dex', 'test']) mkdirSync(join(BUILD, dir), { recursive: true })
run('javac', ['--release', '17', '-d', join(BUILD, 'test'), join(ROOT, 'src', 'app', 'glyphcast', 'Face.java'), join(ROOT, 'test', 'FaceCheck.java')])
run('java', ['-cp', join(BUILD, 'test'), 'app.glyphcast.FaceCheck'])
extract(GLYPH_SDK.file, join(BUILD, 'aar'))
const glyphJar = join(BUILD, 'aar', 'classes.jar')

run(aapt2, ['compile', '--dir', join(ROOT, 'res'), '-o', join(BUILD, 'res.zip')])
run(aapt2, [
  'link', '-o', join(BUILD, 'unsigned.apk'),
  '-I', androidJar,
  '--manifest', join(ROOT, 'AndroidManifest.xml'),
  '--java', join(BUILD, 'gen'),
  '--min-sdk-version', String(MIN_SDK),
  '--target-sdk-version', String(TARGET_SDK),
  '--version-code', VERSION.code,
  '--version-name', VERSION.name,
  join(BUILD, 'res.zip')
])
run('javac', ['--release', '17', '-cp', [androidJar, glyphJar].join(delimiter), '-d', join(BUILD, 'classes'), ...files(join(ROOT, 'src')), ...files(join(BUILD, 'gen'))])
run('java', ['-cp', R8.file, 'com.android.tools.r8.D8', '--release', '--min-api', String(MIN_SDK), '--lib', androidJar, '--output', join(BUILD, 'dex'), glyphJar, ...files(join(BUILD, 'classes'))])
run('jar', ['ufM', join(BUILD, 'unsigned.apk'), '-C', join(BUILD, 'dex'), 'classes.dex'])
run(zipalign, ['-p', '-f', '4', join(BUILD, 'unsigned.apk'), join(BUILD, 'aligned.apk')])

if (!existsSync(KEY.file)) {
  run('keytool', ['-genkeypair', '-keystore', KEY.file, '-alias', KEY.alias, '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000',
    '-storepass', KEY.password, '-keypass', KEY.password, '-dname', 'CN=GlyphCast'])
}
mkdirSync(join(ROOT, 'bin'), { recursive: true })
run('java', ['-jar', apksigner, 'sign', '--v4-signing-enabled', 'false', '--ks', KEY.file, '--ks-key-alias', KEY.alias, '--ks-pass', `pass:${KEY.password}`, '--key-pass', `pass:${KEY.password}`, '--out', OUT, join(BUILD, 'aligned.apk')])
run('java', ['-jar', apksigner, 'verify', OUT])
console.log(`Built ${OUT} (${(statSync(OUT).size / 1e3).toFixed(0)} KB, version ${VERSION.name})`)
