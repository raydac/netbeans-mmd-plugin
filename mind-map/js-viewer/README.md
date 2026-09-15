# MMD browser viewer

Embeddable JavaScript viewer for Scia Reto `.mmd` mind maps. It parses the same Markdown-compatible format as the Java
editors, draws a left/right topic tree with attached pictures, and lets a host page turn chrome, notes, icons, and
interaction on or off so the map fits the page.

## Quick start

```html
<link rel="stylesheet" href="mmd-viewer.css">
<script src="mmd-viewer.js"></script>

<div id="map" style="height: 640px;"></div>
<script>
  MmdViewer.attach("#map", { src: "notes.mmd" });
</script>
```

Or mount from markup. Every `data-mmd-*` attribute maps to an attach option (`data-mmd-toolbar="false"`,
`data-mmd-preset="picture"`, …):

```html
<div data-mmd-viewer data-mmd-src="notes.mmd" data-mmd-preset="embed" style="height: 640px;"></div>
```

Open `index.html` over HTTP (for example `python3 -m http.server` from this folder). The example page loads
`examples/demo.mmd` and lets you switch presets live.

For a smaller drop-in script, use `mmd-viewer.min.js` instead of `mmd-viewer.js`. Rebuild it with:

```bash
python3 pack-js.py
```

That file minifies the viewer and inlines a compacted copy of `mmd-icons.json`, so the page does not fetch the JSON.
Keep `mmd-icons.png` next to the script. `python3 pack-js.py --no-embed-atlas` minifies without embedding the atlas.

## Browsers

Current Chrome, Firefox, Safari, and Edge on desktop, plus iOS Safari and Android Chrome. Pan and pinch use Pointer
Events when the browser has them; otherwise mouse and touch. A `Promise`-capable browser is required.

## Presets

Pass `preset` to start from a layout, then override individual flags.

| Preset    | Use on the page                                                |
|-----------|----------------------------------------------------------------|
| `full`    | Default reader: toolbar, pan/zoom, notes on topic double-click |
| `embed`   | No toolbar; notes panel opens on topic double-click            |
| `map`     | Diagram only (pan/zoom, no notes chrome)                       |
| `picture` | Static illustration: no pan, zoom, fold, or selection          |
| `reader`  | Notes panel stays open; no toolbar                             |

```javascript
MmdViewer.attach("#map", { src: "notes.mmd", preset: "picture" });

const viewer = MmdViewer.attach("#map", { src: "notes.mmd" });
viewer.configure({ preset: "embed" });
viewer.configure({ toolbar: false, notes: true, grid: false });
```

`showNotes`, `showControls`, and `showGrid` still work as aliases for `notes`, `toolbar`, and `grid`.

## Attach options

```javascript
const viewer = MmdViewer.attach(element, {
    src: "map.mmd",
    text: mmdSource,
    preset: "full",
    toolbar: true,
    openFile: true,
    zoomButtons: true,
    fitButton: true,
    notesButton: true,
    hint: true,
    notes: true,
    notesOpen: false,
    notesLayout: "auto",    // auto | landscape | portrait | modal
    grid: true,
    shadows: true,
    jumps: "auto",          // true | false | "auto" (honor the file's showJumps)
    emoticons: true,
    extras: true,
    images: true,
    collapsators: true,
    selection: true,
    pan: true,
    wheelZoom: true,
    pinchZoom: true,
    keyboard: true,
    fold: true,
    followJumps: true,
    followLinks: true,      // extra-icon double-click opens LINK/FILE extras and image URIs
    decryptNotes: true,
    unfoldAll: false,
    fitOnLoad: true,
    interactive: true,      // shortcut for pan, wheelZoom, pinchZoom, keyboard, fold
    icons: true,
    iconsUrl: "mmd-icons.png",
    iconsMetaUrl: "mmd-icons.json",
    config: { /* MindMapPanelConfig-like colors and spacing */}
});

viewer.loadText(mmdSource);
viewer.loadUrl("other.mmd");
viewer.configure({notes: false, pan: true});
viewer.getOptions();
viewer.fit();
viewer.zoomBy(1.2);
viewer.ensureVisible(topic);
viewer.destroy();
```

`MmdViewer.parse(text)` returns the map tree (`header`, `attributes`, `root`) without rendering.
`MmdViewer.optionDefaults` and `MmdViewer.presets` are the library defaults.

## Reader controls

Enabled only when the matching option is on.

| Action                     | How                                                                                                                                                                                                                                                                                                                   |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Pan                        | Drag anywhere. A short click still selects a topic                                                                                                                                                                                                                                                                    |
| Zoom                       | Mouse wheel, toolbar, pinch, or Ctrl/Cmd +/-                                                                                                                                                                                                                                                                          |
| Fit                        | Fit button or Ctrl/Cmd 0                                                                                                                                                                                                                                                                                              |
| Select                     | Click a topic; a clipped topic is scrolled fully into view                                                                                                                                                                                                                                                            |
| Fold / unfold              | Click the circle on a topic, or `-` / `+`                                                                                                                                                                                                                                                                             |
| Move                       | Arrow keys follow the Java editor: from the root, left/right enter that side of the map; up/down stay among siblings. Home selects the root                                                                                                                                                                           |
| Notes                      | Notes button, or double-click a topic (double-tap on touch). Click and drag never open the panel. `notesLayout` is `auto` (side column on a wide screen, bottom sheet in portrait, right overlay in landscape), `landscape`, `portrait`, or `modal` (centered dialog; click the dimmed area or press Escape to close) |
| Jump                       | Double-click the JUMP extra icon, or JUMP in the notes panel. Dashed jump lines end with an arrow on the target                                                                                                                                                                                                       |
| Open a web or file extra   | Double-click the extra icon. The URL or file path overlays the map and does not resize the toolbar                                                                                                                                                                                                                    |
| Open an attached image URI | Double-click the picture on the topic. The stored `mmd.image.uri` overlays the map and opens when the browser can reach it                                                                                                                                                                                            |
| Open a local file          | Toolbar Open                                                                                                                                                                                                                                                                                                          |

Encrypted notes stay locked until the reader types the password. The example map uses the format interoperability
password `hello` (hint is shown in the panel). The password is never written back into the file.

## Files

| File                | Role                                                      |
|---------------------|-----------------------------------------------------------|
| `mmd-viewer.js`     | Parser, layout, SVG renderer, viewer widget               |
| `mmd-viewer.min.js` | Packed viewer with compacted icon atlas inlined           |
| `mmd-viewer.css`    | Host and notes-panel styles                               |
| `mmd-icons.png`     | Sprite of Java panel emoticons and extra icons            |
| `mmd-icons.json`    | Sprite index (`mmd.emoticon` names and extra slots)       |
| `pack-icons.py`     | Rebuilds the sprite from `mind-map-swing-panel` resources |
| `pack-js.py`        | Builds `mmd-viewer.min.js`                                |
| `index.html`        | Example page with live option toggles                     |
| `examples/demo.mmd` | Sample map                                                |
