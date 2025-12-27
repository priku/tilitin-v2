# ====================================================================
# Tilitin 2.0.2 - Moderni Ikoni Generator
# Luo modernit sovellusikonit "Evolution" -tyylillä
# 
# Ominaisuudet:
# - Gradientti (tummansininen -> sininen)
# - Pyöristetyt kulmat
# - Euro-symboli (€)
# - Hienovarainen varjo
# - Grid-viivat (viittaus taulukkoon)
# ====================================================================

Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Drawing.Common

# Väripaletti (IKONI-MODERNISOINTI.md mukaan)
$primaryDark = [System.Drawing.Color]::FromArgb(255, 30, 58, 138)   # #1E3A8A
$primaryLight = [System.Drawing.Color]::FromArgb(255, 59, 130, 246) # #3B82F6
$accentTeal = [System.Drawing.Color]::FromArgb(255, 20, 184, 166)   # #14B8A6
$accentGold = [System.Drawing.Color]::FromArgb(255, 245, 158, 11)   # #F59E0B
$shadowColor = [System.Drawing.Color]::FromArgb(60, 0, 0, 0)        # Läpinäkyvä musta

function New-ModernIcon {
    param(
        [int]$Size,
        [string]$OutputPath
    )
    
    Write-Host "Luodaan $Size x $Size ikoni: $OutputPath"
    
    # Luo bitmap läpinäkyvällä taustalla
    $bitmap = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    
    # Anti-aliasing ja korkea laatu
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    
    # Tyhjennä tausta (läpinäkyvä)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    
    # Mittasuhteet (skaalautuvat koon mukaan)
    $margin = [int]($Size * 0.08)
    $docWidth = [int]($Size * 0.65)
    $docHeight = [int]($Size * 0.80)
    $docX = $margin + [int]($Size * 0.02)
    $docY = $margin
    $cornerRadius = [int]([Math]::Max(2, $Size * 0.08))
    $shadowOffset = [int]([Math]::Max(1, $Size * 0.04))
    
    # --- VARJO (dokumentin alla) ---
    if ($Size -ge 32) {
        $shadowBrush = New-Object System.Drawing.SolidBrush($shadowColor)
        $shadowPath = New-RoundedRectPath ($docX + $shadowOffset) ($docY + $shadowOffset) $docWidth $docHeight $cornerRadius
        $graphics.FillPath($shadowBrush, $shadowPath)
        $shadowBrush.Dispose()
        $shadowPath.Dispose()
    }
    
    # --- DOKUMENTTI (gradientti) ---
    $docRect = New-Object System.Drawing.Rectangle($docX, $docY, $docWidth, $docHeight)
    $gradientBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        $docRect,
        $primaryLight,
        $primaryDark,
        [System.Drawing.Drawing2D.LinearGradientMode]::ForwardDiagonal
    )
    
    $docPath = New-RoundedRectPath $docX $docY $docWidth $docHeight $cornerRadius
    $graphics.FillPath($gradientBrush, $docPath)
    
    # Reunaviiva
    $borderPen = New-Object System.Drawing.Pen($primaryDark, [Math]::Max(1, $Size * 0.02))
    $graphics.DrawPath($borderPen, $docPath)
    $borderPen.Dispose()
    
    # --- GRID-VIIVAT (dokumentin sisällä) ---
    if ($Size -ge 24) {
        $gridColor = [System.Drawing.Color]::FromArgb(80, 255, 255, 255)
        $gridPen = New-Object System.Drawing.Pen($gridColor, [Math]::Max(1, $Size * 0.015))
        
        $gridMargin = [int]($docWidth * 0.15)
        $gridWidth = $docWidth - (2 * $gridMargin)
        $lineCount = 3
        $lineSpacing = [int]($docHeight * 0.15)
        $startY = $docY + [int]($docHeight * 0.25)
        
        for ($i = 0; $i -lt $lineCount; $i++) {
            $lineY = $startY + ($i * $lineSpacing)
            $graphics.DrawLine($gridPen, $docX + $gridMargin, $lineY, $docX + $gridMargin + $gridWidth, $lineY)
        }
        $gridPen.Dispose()
    }
    
    # --- EURO-SYMBOLI (€) oikeassa yläkulmassa ---
    if ($Size -ge 24) {
        $euroSize = [int]($Size * 0.25)
        $euroFont = New-Object System.Drawing.Font("Arial", [Math]::Max(6, $euroSize * 0.8), [System.Drawing.FontStyle]::Bold)
        $euroBrush = New-Object System.Drawing.SolidBrush($accentGold)
        
        $euroX = $docX + $docWidth - [int]($euroSize * 0.7)
        $euroY = $docY + [int]($docHeight * 0.05)
        
        # Pieni varjo euro-symbolille
        if ($Size -ge 48) {
            $euroShadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(100, 0, 0, 0))
            $graphics.DrawString("€", $euroFont, $euroShadowBrush, ($euroX + 1), ($euroY + 1))
            $euroShadowBrush.Dispose()
        }
        
        $graphics.DrawString("€", $euroFont, $euroBrush, $euroX, $euroY)
        $euroFont.Dispose()
        $euroBrush.Dispose()
    }
    
    # --- KYNÄ (turkoosi, oikeassa alakulmassa) ---
    $penLength = [int]($Size * 0.45)
    $penWidth = [int]([Math]::Max(2, $Size * 0.10))
    $penAngle = -45
    
    # Kynän keskikohta
    $penCenterX = $docX + $docWidth - [int]($Size * 0.05)
    $penCenterY = $docY + $docHeight - [int]($Size * 0.05)
    
    # Kynän päätepisteet (45 asteen kulmassa)
    $penStartX = $penCenterX - [int]($penLength * 0.35)
    $penStartY = $penCenterY + [int]($penLength * 0.35)
    $penEndX = $penCenterX + [int]($penLength * 0.35)
    $penEndY = $penCenterY - [int]($penLength * 0.35)
    
    # Kynän runko (gradientti)
    $penPen = New-Object System.Drawing.Pen($accentTeal, $penWidth)
    $penPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Triangle
    $graphics.DrawLine($penPen, $penStartX, $penStartY, $penEndX, $penEndY)
    $penPen.Dispose()
    
    # Kynän kärki (tummempi)
    $tipColor = [System.Drawing.Color]::FromArgb(255, 13, 148, 136) # #0D9488
    $tipPen = New-Object System.Drawing.Pen($tipColor, [Math]::Max(1, $penWidth * 0.5))
    $tipLength = [int]($penLength * 0.15)
    $tipEndX = $penEndX + [int]($tipLength * 0.7)
    $tipEndY = $penEndY - [int]($tipLength * 0.7)
    $graphics.DrawLine($tipPen, $penEndX, $penEndY, $tipEndX, $tipEndY)
    $tipPen.Dispose()
    
    # --- GLOW-EFEKTI kynän kärjessä (isommissa ikoneissa) ---
    if ($Size -ge 48) {
        $glowSize = [int]($Size * 0.06)
        $glowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(150, 255, 255, 200))
        $graphics.FillEllipse($glowBrush, $tipEndX - $glowSize/2, $tipEndY - $glowSize/2, $glowSize, $glowSize)
        $glowBrush.Dispose()
    }
    
    # Siivoa
    $gradientBrush.Dispose()
    $docPath.Dispose()
    $graphics.Dispose()
    
    # Tallenna PNG
    $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
    
    Write-Host "  -> Valmis!" -ForegroundColor Green
}

function New-RoundedRectPath {
    param(
        [int]$X,
        [int]$Y,
        [int]$Width,
        [int]$Height,
        [int]$Radius
    )
    
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    
    if ($Radius -lt 1) {
        $path.AddRectangle((New-Object System.Drawing.Rectangle($X, $Y, $Width, $Height)))
    } else {
        $diameter = $Radius * 2
        
        # Ylävasen kulma
        $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
        # Yläoikea kulma  
        $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
        # Alaoikea kulma
        $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
        # Alavasen kulma
        $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
        
        $path.CloseFigure()
    }
    
    return $path
}

function New-MultiResolutionIco {
    param(
        [string[]]$PngFiles,
        [string]$OutputPath
    )
    
    Write-Host "`nLuodaan multi-resolution .ico: $OutputPath"
    
    # Lue PNG-tiedostot
    $images = @()
    foreach ($pngFile in $PngFiles) {
        if (Test-Path $pngFile) {
            $images += [System.Drawing.Image]::FromFile($pngFile)
        }
    }
    
    if ($images.Count -eq 0) {
        Write-Host "  -> VIRHE: Ei PNG-tiedostoja!" -ForegroundColor Red
        return
    }
    
    # ICO-tiedoston rakenne (yksinkertaistettu - käytä ImageMagick:ia parempaan tulokseen)
    # Tässä luodaan .ico jossa on vain suurin kuva
    $largestImage = $images | Sort-Object Width -Descending | Select-Object -First 1
    
    # Kopioi suurin kuva .ico-muotoon
    $icon = [System.Drawing.Icon]::FromHandle(([System.Drawing.Bitmap]$largestImage).GetHicon())
    
    $fileStream = [System.IO.File]::Create($OutputPath)
    $icon.Save($fileStream)
    $fileStream.Close()
    
    # Siivoa
    foreach ($img in $images) {
        $img.Dispose()
    }
    
    Write-Host "  -> Valmis! (HUOM: Käytä ImageMagick:ia täydelliseen multi-resolution .ico:oon)" -ForegroundColor Yellow
}

# ====================================================================
# PÄÄOHJELMA
# ====================================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Tilitin 2.0.2 - Ikoni Generator" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$resourcePath = "src\main\resources"

# Varmista että hakemisto on olemassa
if (-not (Test-Path $resourcePath)) {
    Write-Host "VIRHE: Hakemistoa $resourcePath ei löydy!" -ForegroundColor Red
    Write-Host "Suorita tämä skripti projektin juurihakemistosta." -ForegroundColor Red
    exit 1
}

# Luo väliaikainen hakemisto
$tempDir = "temp_icons"
if (-not (Test-Path $tempDir)) {
    New-Item -ItemType Directory -Path $tempDir | Out-Null
}

# Luo ikonit eri kooissa
$sizes = @(16, 24, 32, 48, 64, 128, 256)

Write-Host "Luodaan PNG-ikonit..." -ForegroundColor Yellow
Write-Host ""

foreach ($size in $sizes) {
    $outputPath = "$tempDir\tilitin-$size.png"
    New-ModernIcon -Size $size -OutputPath $outputPath
}

Write-Host ""
Write-Host "Kopioidaan ikonit resources-hakemistoon..." -ForegroundColor Yellow

# Kopioi app-ikonit (käytetään sovelluksessa)
Copy-Item "$tempDir\tilitin-16.png" "$resourcePath\app-16x16.png" -Force
Copy-Item "$tempDir\tilitin-32.png" "$resourcePath\app-32x32.png" -Force
Copy-Item "$tempDir\tilitin-48.png" "$resourcePath\app-48x48.png" -Force

# Kopioi tilitin-ikonit (käytetään ikkunoissa)
Copy-Item "$tempDir\tilitin-24.png" "$resourcePath\tilitin-24x24.png" -Force
Copy-Item "$tempDir\tilitin-32.png" "$resourcePath\tilitin-32x32.png" -Force
Copy-Item "$tempDir\tilitin-48.png" "$resourcePath\tilitin-48x48.png" -Force

Write-Host ""
Write-Host "Luodaan .ico tiedosto..." -ForegroundColor Yellow

# Yritä luoda .ico ImageMagick:lla (jos asennettu)
$magickPath = Get-Command "magick" -ErrorAction SilentlyContinue

if ($magickPath) {
    Write-Host "ImageMagick löydetty, luodaan multi-resolution .ico..."
    
    & magick "$tempDir\tilitin-16.png" "$tempDir\tilitin-24.png" "$tempDir\tilitin-32.png" `
             "$tempDir\tilitin-48.png" "$tempDir\tilitin-64.png" "$tempDir\tilitin-128.png" `
             "$tempDir\tilitin-256.png" "$resourcePath\tilitin.ico"
    
    Write-Host "  -> .ico luotu ImageMagick:lla!" -ForegroundColor Green
} else {
    Write-Host "ImageMagick ei löydy, kopioidaan 48x48 PNG .ico-muotoon..." -ForegroundColor Yellow
    
    # Luo yksinkertainen .ico 48x48 kuvasta
    $bitmap = [System.Drawing.Bitmap]::FromFile("$tempDir\tilitin-48.png")
    $icon = [System.Drawing.Icon]::FromHandle($bitmap.GetHicon())
    
    $fs = [System.IO.File]::Create("$resourcePath\tilitin.ico")
    $icon.Save($fs)
    $fs.Close()
    
    $bitmap.Dispose()
    
    Write-Host "  -> Yksinkertainen .ico luotu." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "SUOSITUS: Asenna ImageMagick paremman .ico-tiedoston luomiseksi:" -ForegroundColor Cyan
    Write-Host "  winget install ImageMagick.ImageMagick" -ForegroundColor White
}

# Siivoa väliaikaiset tiedostot
Write-Host ""
Write-Host "Siivotaan väliaikaiset tiedostot..."
Remove-Item $tempDir -Recurse -Force

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  VALMIS!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Luodut tiedostot:" -ForegroundColor White
Write-Host "  - $resourcePath\app-16x16.png" 
Write-Host "  - $resourcePath\app-32x32.png"
Write-Host "  - $resourcePath\app-48x48.png"
Write-Host "  - $resourcePath\tilitin-24x24.png"
Write-Host "  - $resourcePath\tilitin-32x32.png"
Write-Host "  - $resourcePath\tilitin-48x48.png"
Write-Host "  - $resourcePath\tilitin.ico"
Write-Host ""
Write-Host "Seuraavat vaiheet:" -ForegroundColor Cyan
Write-Host "  1. Tarkista ikonit visuaalisesti"
Write-Host "  2. mvn clean package"
Write-Host "  3. build-windows.bat"
Write-Host "  4. Testaa sovellus"
Write-Host ""
