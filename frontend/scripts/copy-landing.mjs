import { createHash } from 'node:crypto';
import { cp, mkdir, readFile, readdir, writeFile } from 'node:fs/promises';

// Keep standalone sources buildless. Published assets get a content version so
// CDN caches cannot mix a new index.html with old CSS or imported JS modules.
const source = new URL('../../landing/', import.meta.url);
const target = new URL('../dist/landing/', import.meta.url);
const hash = createHash('sha256');
async function hashDirectory(directory, prefix = '') {
  const entries = await readdir(directory, { withFileTypes: true });
  entries.sort((a, b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
  for (const entry of entries) {
    const path = prefix + entry.name;
    if (entry.isDirectory()) {
      await hashDirectory(new URL(entry.name + '/', directory), path + '/');
    } else {
      hash.update(path + '\0').update(await readFile(new URL(entry.name, directory))).update('\0');
    }
  }
}
await hashDirectory(source);
const version = hash.digest('hex').slice(0, 16);
await mkdir(target, { recursive: true });
await cp(new URL('assets/', source), new URL(`assets/${version}/`, target), { recursive: true });
const html = (await readFile(new URL('index.html', source), 'utf8'))
  .replace(/((?:src|href)=["'])assets\//g, `$1assets/${version}/`);
await writeFile(new URL('index.html', target), html);
console.log(`Landing asset version: ${version}`);
