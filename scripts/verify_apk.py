"""Fail the build if the release certificate changes or APK signatures are invalid."""
import os
from pathlib import Path
import re
import subprocess
import sys

expected = Path('signing-certificate.sha256').read_text().strip().lower()
if not re.fullmatch(r'[0-9a-f]{64}', expected):
    raise SystemExit('Invalid pinned signing certificate fingerprint')
sdk = Path(os.environ.get('ANDROID_HOME') or os.environ['ANDROID_SDK_ROOT'])
apksigner = sdk / 'build-tools' / '35.0.0' / 'apksigner'
result = subprocess.run([str(apksigner), 'verify', '--verbose', '--print-certs', sys.argv[1]], check=True, text=True, capture_output=True)
actual = re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-fA-F]+)', result.stdout)
if not actual or actual.group(1).lower() != expected:
    raise SystemExit('SIGNING CERTIFICATE CHANGED: refusing to publish an incompatible APK')
if 'Verified using v1 scheme (JAR signing): true' not in result.stdout:
    raise SystemExit('v1 signature missing; Android 5/6 compatibility requires it')
if 'Verified using v2 scheme (APK Signature Scheme v2): true' not in result.stdout:
    raise SystemExit('v2 signature missing')
print(result.stdout)
print('Release signature matches the pinned permanent certificate.')
