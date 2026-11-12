"""Draw the plant fiber item by hand on a transparent 32-pixel canvas."""

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
TEXTURE = ROOT / "src/main/resources/assets/project-medica/textures/item/plant_fiber.png"
PREVIEW = Path(__file__).with_name("plant-fiber-preview.png")

image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
draw = ImageDraw.Draw(image)

outline = "#54543A"
shadow = "#828052"
green = "#A7AA70"
straw = "#C5BE80"
light = "#E2D69A"
highlight = "#F1E4B2"

# Unbound bast strands: separate, uneven ends rather than a twisted rope.
strands = [
    ([(5, 24), (7, 19), (10, 15), (13, 13), (16, 9), (18, 5), (21, 3)], green),
    ([(4, 20), (8, 18), (12, 15), (16, 12), (20, 8), (25, 6)], straw),
    ([(8, 28), (10, 24), (11, 20), (15, 16), (19, 12), (23, 9), (28, 7)], shadow),
    ([(5, 27), (8, 23), (12, 19), (15, 15), (19, 11), (22, 6), (24, 3)], light),
    ([(11, 28), (12, 24), (15, 20), (17, 16), (20, 13), (24, 11), (28, 10)], green),
    ([(3, 25), (7, 24), (11, 21), (14, 17), (18, 14), (23, 12), (27, 12)], straw),
]

for points, color in strands:
    draw.line(points, fill=outline, width=3)
    draw.line(points, fill=color, width=1)

# Long pale filaments on the face of the loose bundle.
draw.line([(7, 25), (10, 22), (13, 18), (16, 15), (19, 12)], fill=light)
draw.line([(10, 24), (12, 21), (15, 18), (17, 14), (20, 11)], fill=straw)
draw.line([(12, 20), (15, 16), (18, 13), (21, 10)], fill=highlight)
draw.line([(15, 21), (17, 17), (21, 14), (24, 13)], fill=light)
draw.line([(12, 16), (15, 13), (18, 9)], fill=light)

# Fine frays remain attached to their strands, with no binding or seed heads.
draw.line([(21, 3), (22, 2)], fill=green)
draw.line([(24, 3), (24, 1)], fill=straw)
draw.line([(25, 6), (27, 4)], fill=light)
draw.line([(28, 7), (30, 6)], fill=shadow)
draw.line([(28, 10), (30, 10)], fill=green)
draw.line([(5, 24), (3, 23), (2, 23)], fill=green)
draw.line([(5, 27), (3, 29)], fill=straw)
draw.line([(8, 28), (7, 30)], fill=light)
draw.line([(11, 28), (12, 30)], fill=green)

TEXTURE.parent.mkdir(parents=True, exist_ok=True)
image.save(TEXTURE)

# Preview background is kept out of the actual texture.
preview = Image.new("RGBA", (320, 320), "#30383E")
preview.alpha_composite(image.resize((320, 320), Image.Resampling.NEAREST))
preview.convert("RGB").save(PREVIEW)
print(f"Texture: {TEXTURE}")
print(f"Preview: {PREVIEW}")
