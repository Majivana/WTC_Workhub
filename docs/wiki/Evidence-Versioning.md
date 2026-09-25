# Evidence Versioning

An evidence item has a history of versions. A requested change creates a new version instead
of overwriting the old object.

Each version retains its checksum, private object reference, uploader, upload timestamp, and
optional change notes. Uploading a replacement also creates an audit event; prior versions and
their metadata remain unchanged.

Verification records reference the exact evidence version reviewed.
