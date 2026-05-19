const fs = require('fs');
const path = require('path');

const SIZES = {
  'mdpi': 108,
  'hdpi': 162,
  'xhdpi': 216,
  'xxhdpi': 324,
  'xxxhdpi': 432,
};

const OUTPUT_DIR = path.join(__dirname, '..', 'app', 'src', 'main', 'res');

async function main() {
  const { createCanvas } = require('canvas');

  for (const [density, size] of Object.entries(SIZES)) {
    const canvas = createCanvas(size, size);
    const ctx = canvas.getContext('2d');

    const padding = size * 0.15;
    const drawSize = size - padding * 2;

    ctx.fillStyle = '#FFF5E6';
    ctx.fillRect(0, 0, size, size);

    ctx.fillStyle = '#8B7355';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    const fontSize = Math.round(drawSize * 0.65);
    ctx.font = `bold ${fontSize}px "ZCOOL QingKe HuangYou"`;

    ctx.fillText('回声', size / 2, size / 2 + drawSize * 0.05);

    const outDir = path.join(OUTPUT_DIR, `mipmap-${density}`);
    if (!fs.existsSync(outDir)) {
      fs.mkdirSync(outDir, { recursive: true });
    }

    const outPath = path.join(outDir, 'ic_echo_foreground.png');
    const buffer = canvas.toBuffer('image/png');
    fs.writeFileSync(outPath, buffer);
    console.log(`✓ ${density} (${size}px)`);
  }

  console.log('\nDone! All icons generated.');
}

main().catch(err => {
  console.error('Error:', err.message);
  process.exit(1);
});
