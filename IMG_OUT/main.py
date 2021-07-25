from PIL import Image as img
import os

print("<Python part>")
im = img.open(os.path.abspath('./IMG_OUT/img.ppm'))
im.save(os.path.abspath('./IMG_OUT/img.png'))
im = im.resize((1024,1024), resample=img.NEAREST)
im.show()
print("</Python part>")
