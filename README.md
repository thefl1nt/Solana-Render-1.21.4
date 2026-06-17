# Solana Render System

Lightweight rendering system for Minecraft 1.21.4 based on OpenGL and GLSL fragment shaders.

## Features

### MSDF Text Rendering

High-quality text rendering using Multi-channel Signed Distance Fields (MSDF).

- Crisp text at any scale
- Smooth edges
- Alpha blending support
- Color customization
- Shadow support

### Liquid Glass

Modern iOS-style liquid glass effect.

- Dynamic background refraction
- Smooth transparency
- Frosted glass appearance
- Soft light scattering
- Rounded panel support

### Kawase Blur

Fast and efficient blur implementation.

- Two-pass Kawase Blur
- Adjustable radius
- Smooth results

## Included Shaders

```text
shaders/
├── blur
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── border
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── corner
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── gradient_rectangle
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── kawase_down
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── kawase_up
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── liquidglass
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── msdf_font
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── rectangle
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── squircle
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
├── squircle_texture
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
└── texture
    ├── data.json
    ├── fragment.fsh
    └── vertex.vsh
```

## Effects

### Kawase Blur

GPU-accelerated blur used for:

- Menus
- ClickGUI
- Windows
- Background effects

### Liquid Glass

Inspired by Apple's Liquid Glass design.

Used for:

- Panels
- HUD elements
- Popups
- Modern interfaces

### MSDF Text

Distance-field text renderer providing:

- Sharp glyph rendering
- Resolution independence
- Smooth scaling
- Consistent readability

## Requirements

- Minecraft 1.21.4
- OpenGL 3.2+
- GLSL 150+

## Technologies

- OpenGL
- GLSL
- Framebuffers
- Kawase Blur
- MSDF Fonts
- Liquid Glass

## License

MIT