# PowerShell-skripti Windows .ico-tiedoston luomiseksi
# Luo tilitin.ico PNG-kuvista

Add-Type -AssemblyName System.Drawing

$sourcePng = "src\main\resources\tilitin-48x48.png"
$outputIco = "src\main\resources\tilitin.ico"

# Lue lähde PNG
$sourceImage = [System.Drawing.Image]::FromFile((Resolve-Path $sourcePng))

# Luo kuvat eri resoluutioilla
$sizes = @(16, 32, 48, 64, 128, 256)
$images = @()

foreach ($size in $sizes) {
    $bitmap = New-Object System.Drawing.Bitmap $size, $size
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)

    # Korkealaatuinen skaalaus
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $graphics.DrawImage($sourceImage, 0, 0, $size, $size)
    $graphics.Dispose()

    $images += $bitmap
}

# Tallenna .ico-tiedosto
$fileStream = [System.IO.File]::Create($outputIco)
$iconWriter = New-Object System.IO.BinaryWriter($fileStream)

# ICO header
$iconWriter.Write([UInt16]0)  # Reserved
$iconWriter.Write([UInt16]1)  # Image type (1 = ICO)
$iconWriter.Write([UInt16]$images.Count)  # Number of images

# Image directory
$offset = 6 + ($images.Count * 16)

foreach ($image in $images) {
    # ICO-formaatissa 256 = 0
    $w = if ($image.Width -eq 256) { 0 } else { $image.Width }
    $h = if ($image.Height -eq 256) { 0 } else { $image.Height }
    $iconWriter.Write([Byte]$w)
    $iconWriter.Write([Byte]$h)
    $iconWriter.Write([Byte]0)  # Color palette
    $iconWriter.Write([Byte]0)  # Reserved
    $iconWriter.Write([UInt16]1)  # Color planes
    $iconWriter.Write([UInt16]32)  # Bits per pixel

    $ms = New-Object System.IO.MemoryStream
    $image.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    $imageBytes = $ms.ToArray()
    $ms.Dispose()

    $iconWriter.Write([UInt32]$imageBytes.Length)
    $iconWriter.Write([UInt32]$offset)
    $offset += $imageBytes.Length
}

# Image data
foreach ($image in $images) {
    $ms = New-Object System.IO.MemoryStream
    $image.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    $imageBytes = $ms.ToArray()
    $iconWriter.Write($imageBytes)
    $ms.Dispose()
    $image.Dispose()
}

$iconWriter.Close()
$fileStream.Close()
$sourceImage.Dispose()

Write-Host "tilitin.ico luotu onnistuneesti!" -ForegroundColor Green
Write-Host "Resoluutiot: 16x16, 32x32, 48x48, 64x64, 128x128, 256x256"
