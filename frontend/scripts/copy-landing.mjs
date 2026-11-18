import { cp, mkdir } from 'node:fs/promises';

// Copy the standalone site byte-for-byte. Its relative assets/ URLs need /landing/.
const source = new URL('../../landing/', import.meta.url);
const target = new URL('../dist/landing/', import.meta.url);
await mkdir(target, { recursive: true });
await cp(new URL('index.html', source), new URL('index.html', target));
await cp(new URL('assets/', source), new URL('assets/', target), { recursive: true });
