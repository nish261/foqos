from PIL import Image, ImageDraw, ImageFont
import os

def create_icon(size, is_round=False):
    # Create image
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Background - Deep Purple to Blue Gradient
    # Simulating simple gradient by drawing circle/rect
    # #6750A4 is the primary color we used (Material 3 Purple)
    primary_color = (103, 80, 164, 255) # #6750A4
    
    if is_round:
        draw.ellipse([(0, 0), (size, size)], fill=primary_color)
    else:
        # Round rect for squircle adaptation
        draw.rectangle([(0,0), (size, size)], fill=primary_color)

    # Draw "F"
    # Simple geometric F construction
    padding = size // 4
    thickness = size // 8
    
    # Vertical line
    draw.rectangle(
        [(padding, padding), (padding + thickness, size - padding)],
        fill=(255, 255, 255, 255)
    )
    
    # Top horizontal
    draw.rectangle(
        [(padding, padding), (size - padding, padding + thickness)],
        fill=(255, 255, 255, 255)
    )
    
    # Middle horizontal
    middle_y = size // 2 - thickness // 2
    draw.rectangle(
        [(padding, middle_y), (size - padding - thickness, middle_y + thickness)],
        fill=(255, 255, 255, 255)
    )
    
    return img

sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

base_path = 'app/src/main/res'

for folder, size in sizes.items():
    path = os.path.join(base_path, folder)
    if not os.path.exists(path):
        os.makedirs(path)
        
    # Standard Icon
    icon = create_icon(size, is_round=False)
    icon.save(os.path.join(path, 'ic_launcher.png'))
    
    # Round Icon
    round_icon = create_icon(size, is_round=True)
    round_icon.save(os.path.join(path, 'ic_launcher_round.png'))

print("Icons generated!")
