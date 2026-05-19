from PIL import Image, ImageDraw, ImageFont
import os

SIZES = {
    'mdpi': 108,
    'hdpi': 162,
    'xhdpi': 216,
    'xxhdpi': 324,
    'xxxhdpi': 432,
}

base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
font_path = os.path.join(base, 'app', 'src', 'main', 'res', 'font', 'zcool_qlkh_yellow_you.ttf')
out_base = os.path.join(base, 'app', 'src', 'main', 'res')

font = ImageFont.truetype(font_path, 100)

for density, size in SIZES.items():
    img = Image.new('RGBA', (size, size), '#FFF5E6')
    draw = ImageDraw.Draw(img)

    font_size = int(size * 0.20)
    font = ImageFont.truetype(font_path, font_size)

    bbox = draw.textbbox((0, 0), '回声', font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    x = (size - tw) // 2
    y = (size - th) // 2

    draw.text((x, y), '回声', fill='#8B7355', font=font)

    out_dir = os.path.join(out_base, f'mipmap-{density}')
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, 'ic_echo_foreground.png')
    img.save(out_path, 'PNG')
    print(f'OK {density} ({size}px)')

print('Done!')
