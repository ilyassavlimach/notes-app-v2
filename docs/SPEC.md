# Notes v2 — Specification
Package: com.machinarium.notesv2 · App prefix: NotesV2 · minSdk 26 · compile/target SDK 37
Languages: en (default), tr (system per-app language, no in-app picker) · Design source: none (Material 3, text
wireframes below) · API: https://jsonplaceholder.typicode.com/

## Summary
A personal notes app that works offline. The first launch seeds the list from JSONPlaceholder `/posts`. After that,
users read, create, edit and delete notes locally, and pull to refresh to update the seeded notes. Notes the user
created or edited, and notes the user deleted, are never overwritten or brought back by a refresh. The UI is in
English and Turkish.

## Features
| Feature module | Purpose | Screens |
|---|---|---|
| noteslist | browse notes, pull to refresh, add a note, undo a delete, notification rationale | NotesList |
| notedetail | read a note, open the editor, delete with confirmation | NoteDetail |
| noteeditor | create a note or edit one, with validation and an unsaved-changes warning | NoteEditor |

## Screens
### NotesList  (feature/noteslist · design: text wireframe)
```
┌──────────────────────────────┐
│ TopAppBar: "Notes"           │
├──────────────────────────────┤
│ ┌ Notification rationale ──┐ │  ← only when permission not granted and ≥ 1 user note
│ │ text · [Not now] [Allow] │ │
│ └──────────────────────────┘ │
│ ┌──────────────────────────┐ │
│ │ Title (titleMedium, 1 ln)│ │  ← NoteListItem (NotesV2Card), whole card clickable
│ │ Body preview (2 lines)   │ │
│ └──────────────────────────┘ │
│ … LazyColumn, key = note.id  │
│                        [ + ] │  ← FAB "Add note"
└──────────────────────────────┘
 PullToRefreshBox wraps the list
```
- Content: visible notes, newest edited first (`updatedAt DESC, id ASC`; seeded notes keep their API order).
- States: loading (first load, empty cache) · content · empty (no notes; text + FAB) · error + Retry (empty cache
  and the seed failed) · refresh-error (cache shown → snackbar "Couldn't refresh, showing saved notes") ·
  deleted (snackbar "Note deleted" + Undo, driven by `NotesRepository.recentlyDeleted`).
- Actions: tap note → NoteDetail(noteId) · FAB → NoteEditor(null) · pull → `refresh()` · Undo → `restoreNote(id)` ·
  rationale Allow → `rememberNotificationPermissionState().request()` · Not now → hidden for this session
  (`rememberSaveable`).
- Analytics: none on this screen (opening a note is tracked in NoteDetail).
- Components: NotesV2TopAppBar, NotesV2FloatingActionButton, NoteListItem (NotesV2Card), NotificationRationaleCard,
  LoadingState, EmptyState, ErrorState (core:ui).
- Strings: noteslist_title, noteslist_empty, noteslist_add_note, noteslist_refresh_failed, noteslist_note_deleted,
  noteslist_undo, noteslist_notifications_rationale, noteslist_notifications_allow, noteslist_notifications_not_now.

### NoteDetail  (feature/notedetail · design: text wireframe)
```
┌──────────────────────────────┐
│ ← TopAppBar: "Note"   ✎  🗑  │
├──────────────────────────────┤
│ Title (headlineSmall)        │
│                              │
│ Body (bodyLarge), scrollable │
└──────────────────────────────┘
 Delete → AlertDialog "Delete this note?" [Cancel] [Delete]
```
- Content: the note read from Room by id (works offline).
- States: loading · content · not-found (message; back in the top bar; also shown for a deleted note) ·
  error + Retry (cache read failed) · delete-confirm dialog.
- Actions: back → previous · edit → NoteEditor(noteId) · delete → confirm → `deleteNote(id)` → back to the list,
  which shows the undo snackbar.
- Analytics: `note_opened {note_id}` once per screen open · `note_deleted {note_id}` on confirm.
- Components: NotesV2TopAppBar (navigation icon + actions), NotesV2AlertDialog, LoadingState, ErrorState.
- Strings: notedetail_title, notedetail_not_found, notedetail_edit, notedetail_delete,
  notedetail_delete_confirm_title, notedetail_delete_confirm_body, common_back, common_cancel (strings_common).

### NoteEditor  (feature/noteeditor · design: text wireframe)
```
┌──────────────────────────────┐
│ ✕ TopAppBar: "New note" /    │
│   "Edit note"         [Save] │
├──────────────────────────────┤
│ OutlinedTextField Title      │  ← required, max 100 chars, counter + error text
│ OutlinedTextField Body       │  ← max 5 000 chars, multi-line, fills the rest
└──────────────────────────────┘
 Back / ✕ with changes → AlertDialog "Discard changes?" [Keep editing] [Discard]
```
- Content: empty fields (create) or the note's current title/body (edit). Field text survives rotation and process
  death (`SavedStateHandle`).
- States: loading (edit only) · editing (Save enabled when the title isn't blank, both limits hold and something
  changed) · saving · not-found (edit of a missing note) · save-error (snackbar, fields kept) ·
  discard-confirm dialog.
- Actions: Save → `createNote` / `updateNote` → close the editor (create → back to the list; edit → back to the
  detail) · back/✕ → close, or the discard dialog if there are unsaved changes.
- Analytics: `note_created {note_id}` · `note_edited {note_id}` after a successful save.
- Components: NotesV2TopAppBar, NotesV2TextField, NotesV2TextButton, NotesV2AlertDialog, LoadingState.
- Strings: noteeditor_title_new, noteeditor_title_edit, noteeditor_save, noteeditor_field_title,
  noteeditor_field_body, noteeditor_error_title_required, noteeditor_error_too_long, noteeditor_not_found,
  noteeditor_save_failed, noteeditor_discard_title, noteeditor_discard_body, noteeditor_discard,
  noteeditor_keep_editing, common_close (strings_common).

## Data
- Models: `Note(id: Long, title: String, body: String)` (ordering is done by Room, so the UI needs no timestamp).
- API: GET /posts → `List<NoteDto(userId, id, title, body)>` (no auth). JSONPlaceholder's write endpoints don't
  save anything, so create/edit/delete stay local; no write calls are made.
- Local: Room `NoteEntity(id PK autoGenerate, remoteId: Long? unique index, title, body, updatedAt: Long,
  syncState: SYNCED | LOCAL, deleted: Boolean)`.
  - Seeded rows: `remoteId = post id`, `SYNCED`, `updatedAt = 0` (so they keep the API order under user notes).
  - Create: `remoteId = null`, `LOCAL`. Edit: becomes `LOCAL`. Delete: `deleted = true` (a tombstone, so a refresh
    can't bring a seeded note back); Undo sets it back to `false`.
  - Refresh, in one transaction: upsert each post by `remoteId` only where the row is missing or `SYNCED` and not
    deleted; delete `SYNCED`, non-deleted rows whose `remoteId` is no longer returned. `LOCAL` and deleted rows are
    never touched.
  - `NoteDao`: observeVisible, observeById, insert, update, setDeleted, findByRemoteIds, upsertSynced,
    deleteSyncedNotIn, `@Transaction` mergeRemote. Schema exported (v1).
  - DataStore: none.
- Time: an injected `java.time.Clock` (`:core:common`), fixed in tests.
- Repository: `NotesRepository`
  - `observeNotes(): Flow<List<Note>>` · `observeNote(id): Flow<Note?>`
  - `refresh(): Result<Unit>` · `createNote(title, body): Result<Long>` · `updateNote(id, title, body): Result<Unit>`
  - `deleteNote(id): Result<Unit>` · `restoreNote(id): Result<Unit>`
  - `recentlyDeleted: StateFlow<Long?>` + `consumeRecentlyDeleted()` (the list's undo snackbar).
  Offline-first: the UI observes Room; the network only writes into Room. `FakeNotesRepository` in `:core:testing`.

## Navigation
Navigation 3, `NavDisplay` in `:app`:
- `NotesListKey` (start) → `NoteDetailKey(noteId)` → `NoteEditorKey(noteId)` → back to detail.
- `NotesListKey` → `NoteEditorKey(null)` → back to the list.
- Delete in NoteDetail → `goBack()` to the list.
- Key arguments reach ViewModels through assisted injection.
- Deep links: `https://<host>/notes/{id}` → `[NotesListKey, NoteDetailKey(id)]`. Anything else → `[NotesListKey]`.
  The host comes from the flavor. `DeepLinkParser` in `:app` handles both App Links and notification taps (data
  key `deeplink`).

## Environments
| Flavor | applicationId | API base URL | Deep-link host |
|---|---|---|---|
| stage | com.machinarium.notesv2.stage | https://jsonplaceholder.typicode.com/ | stage.notes.example.com |
| prod | com.machinarium.notesv2 | https://jsonplaceholder.typicode.com/ | notes.example.com |
The deep-link hosts are placeholders until there is a real domain with `/.well-known/assetlinks.json` (handover).

## Integrations
| Integration | Provider | Module | Why |
|---|---|---|---|
| Crash reporting | Firebase Crashlytics | :app plugin + release `CrashReportingTree` | crashes and logged WARN+ in release |
| Analytics | Firebase Analytics + Adjust + AppsFlyer | :core:analytics (composite, `@IntoSet` providers) | note_opened / note_created / note_edited / note_deleted; Adjust & AppsFlyer stay off until their keys are set |
| Push | FCM | :core:notifications | notifications that open a note through the deep-link parser; rationale card in NotesList (PUSH-01). No backend yet → `PushTokenSink` is not bound (handover) |
| Remote config | Firebase Remote Config | :core:config + `AppGateViewModel` in :app | force update (`min_version_code`) and maintenance mode (`maintenance_mode`) |
| Deep links | https App Links, `autoVerify` | :app (`DeepLinkParser`, manifest placeholders) | open a note from a link or a notification |
Always on: Navigation 3, Timber, Chucker (debug), LeakCanary (debug), App Startup (SDK init).
`app/google-services.json`: a placeholder with both applicationIds until the real file is provided (release blocker).

## Optional modules
- core/network — the seed API (JSONPlaceholder).
- core/database (Room) — the source of truth, offline.
- core/data — repository (always rendered).
- core/analytics, core/notifications, core/config — the integrations above.
- Not used: coil (no images), paging (100 seeded items), work (no sync jobs), datastore (no settings or tokens).

## Hardening
| # | Item | Applies | How / why N/A |
|---|---|---|---|
| 1 | Certificate pinning | yes | OkHttp `CertificatePinner` from `api.<flavor>.certPins`: intermediate + root of jsonplaceholder.typicode.com read from the live chain in E1, plus backup roots from other CAs (system trust store). Source and date recorded here in E1. |
| 2 | Encryption at rest | N/A | no tokens, credentials or personal data; notes are user text the user chose to keep on the device, protected by app sandbox + backup rules |
| 3 | R8 + keep rules | yes | minify + shrinkResources, project proguard-rules.pro |
| 4 | Play Integrity | N/A | no sensitive data or operations |
| 5 | FLAG_SECURE | N/A | no auth/payment/personal-data screens |
| 6 | Biometric | N/A | no login or payments |
| 7 | Signing from env | yes | release signingConfig reads KEYSTORE_* / KEY_* env vars |
| 8 | Dependency scan in CI | yes | OWASP dependency-check workflow (NVD_API_KEY secret) |
Sensitive-app defaults (SEC-07): no.

## Version notes
(filled in Stage C if anything is held back)

## Known exceptions
- noteslist and notedetail are one commit: with only the list screen, the app template's `navigateTo` has no
  caller and the dead-code gate can't pass, so the first feature waited for the second (skill lesson).
