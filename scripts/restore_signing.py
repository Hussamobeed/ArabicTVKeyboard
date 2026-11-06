"""Restore a signing bundle from one GitHub Actions secret; never create/fallback to a key."""
import base64
import json
import os
from pathlib import Path


def restore():
    raw = os.environ.get('ANDROID_SIGNING_BUNDLE', '')
    if not raw:
        raise SystemExit('Missing ANDROID_SIGNING_BUNDLE. Restore the ORIGINAL signing backup in Settings > Secrets and variables > Actions. No new key will be generated.')
    bundle = json.loads(raw)
    key = base64.b64decode(bundle['keystore'], validate=True)
    password = bundle['password']
    alias = bundle['alias']
    if not key or not password or not alias or any(c in password + alias for c in '\r\n'):
        raise SystemExit('Invalid signing bundle')
    directory = Path(os.environ['RUNNER_TEMP']) / 'arabictv-signing'
    directory.mkdir(mode=0o700, exist_ok=True)
    keystore = directory / 'release.p12'
    keystore.write_bytes(key)
    keystore.chmod(0o600)
    # Prevent accidental disclosure if a downstream tool prints its environment.
    print(f'::add-mask::{password}')
    print(f'::add-mask::{alias}')
    with open(os.environ['GITHUB_ENV'], 'a') as output:
        output.write(f'ANDROID_KEYSTORE_PATH={keystore}\nANDROID_STORE_PASSWORD={password}\nANDROID_KEY_PASSWORD={password}\nANDROID_KEY_ALIAS={alias}\n')
    print('Restored existing signing identity; certificate is checked after signing.')


if __name__ == '__main__':
    restore()
