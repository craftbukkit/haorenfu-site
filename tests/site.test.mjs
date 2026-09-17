import test from 'node:test';import assert from 'node:assert/strict';import fs from 'node:fs';
test('core source files exist',()=>{for(const f of['app/page.tsx','app/play/page.tsx','app/mods/page.tsx','app/download/page.tsx','lib/site-data.ts','lib/mods.ts','LICENSE','SECURITY.md'])assert.equal(fs.existsSync(f),true,f)});
test('production secrets are not expected in source',()=>{const text=fs.readFileSync('README.md','utf8');assert.match(text,/生产/);assert.match(text,/MIT/)});
