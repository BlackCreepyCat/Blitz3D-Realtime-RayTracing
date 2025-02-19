Graphics 320, 240, 32, 2
SetBuffer BackBuffer()
SeedRnd MilliSecs()

;Raytracer
InitRaytracer()
SetCameraPosition(0, -25, -230)
SetLightPosition(35, 200, -35)
SetAmbientLight(255)
CreateScene()


Local Px#
Local Py#
Local Pz#
Local Speed#=5
Local ShadowDarkness=100

While Not KeyHit(1)
	If KeyDown(203) Then
		Px# = Px# - Speed#
	EndIf

	If KeyDown(205) Then
		Px#= Px# + Speed#
	EndIf

	If KeyDown(201) Then
		Py#=Py#+Speed#
	EndIf

	If KeyDown(209) Then
		Py#=Py#-Speed#
	EndIf


	If KeyDown(200) Then
		Pz# = Pz# + Speed#
	EndIf

	If KeyDown(208) Then
		Pz# = Pz# - Speed#
	EndIf

	SetCameraPosition(Px#, Py#, Pz#)
	
	Raytrace()
	Flip
Wend

End


Function CreateScene()
AddEntity("Plane", 1, 100, $857830)               ;Floor
AddEntity("Sphere", 100, 10, 100, 50, $660000)   ;Red ball
AddEntity("Sphere", 0, 10, 125, 50, $006600) ;Green ball
AddEntity("Sphere", -100, 10, 100, 50, $000066)    ;Blue ball
End Function


Type Plane
	Field nx#, ny#, nz#
	Field dis#
	Field argb
End Type

Type Sphere
	Field x#, y#, z#
	Field rad#
	Field argb
End Type

Dim normalx#(-1, -1)
Dim normaly#(-1, -1)
Dim normalz#(-1, -1)

Global camerax#, cameray#, cameraz#
Global lightx#, lighty#, lightz#
Global ambientcol

Function InitRaytracer()
	w = GraphicsWidth()
	h = GraphicsHeight()
	Dim normalx#(w, h)
	Dim normaly#(w, h)
	Dim normalz#(w, h)
	SetupNormals()
End Function

Function Raytrace()
Cls
LockBuffer()
	width = GraphicsWidth()
	height = GraphicsHeight()
	For y = 0 To height - 1
		For x = 0 To width - 1
			rgb = SendRay(camerax#, cameray#, cameraz#, normalx#(x, y), normaly#(x, y), normalz#(x, y), ambientcol)
			If rgb <> $000000 Then WritePixelFast x, y, rgb, BackBuffer()
		Next
	Next
	UnlockBuffer()
End Function

Function SendRay(ex#, ey#, ez#, evx#, evy#, evz#, c = 255, recursive_times = 4)
	If recursive_times < 0 Then Return
	Local z# = 10000
	
	For p.Plane = Each Plane
		nx# = p\nx#
		ny# = p\ny#
		nz# = p\nz#
		dis# = nx# * evx# + ny# * evy# + nz# * evz#
		If dis# < 0 Then
			dis2# = -(p\dis# + nx# * ex# + ny# * ey# + nz# * ez#) / dis#
			ix# = ex# + evx# * dis2#
			iy# = ey# + evy# * dis2#
			iz# = ez# + evz# * dis2#
			lvx# = lightx# - ix#
			lvy# = lighty# - iy#
			lvz# = lightz# - iz#
			dis# = Sqr(lvx# * lvx# + lvy# * lvy# + lvz# * lvz#)
			lvx# = lvx# / dis#
			lvy# = lvy# / dis#
			lvz# = lvz# / dis#
			dis# = (nx# * evx# + ny# * evy# + nz# * evz#) * 2
			rnx# = evx# - nx# * dis#
			rny# = evy# - ny# * dis#
			rnz# = evz# - nz# * dis#
			c1 = (nx# * lvx# + ny# * lvy# + nz# * lvz#) * 256
			argb = p\argb
			z# = dis2#
		EndIf
	Next
	
	For s.Sphere = Each Sphere
		svx# = s\x# - ex#
		svy# = s\y# - ey#
		svz# = s\z# - ez#
		dis2# = svx# * evx# + svy# * evy# + svz# * evz#
		dis# = svx# * svx# + svy# * svy# + svz# * svz# - dis2# * dis2#
		If dis# <= s\rad# * s\rad# Then
			dis2# = dis2# - Sqr(s\rad# * s\rad# - dis#)
			If dis2# > 0 And dis2# < z# Then
				ix# = ex# + evx# * dis2#
				iy# = ey# + evy# * dis2#
				iz# = ez# + evz# * dis2#
				nx# = Float(ix# - s\x#) / s\rad#
				ny# = Float(iy# - s\y#) / s\rad#
				nz# = Float(iz# - s\z#) / s\rad#
				lvx# = lightx# - ix#
				lvy# = lighty# - iy#
				lvz# = lightz# - iz#
				dis# = Sqr(lvx# * lvx# + lvy# * lvy# + lvz# * lvz#)
				lvx# = lvx# / dis#
				lvy# = lvy# / dis#
				lvz# = lvz# / dis#
				dis# = (nx# * evx# + ny# * evy# + nz# * evz#) * 2
				rnx# = evx# - nx# * dis#
				rny# = evy# - ny# * dis#
				rnz# = evz# - nz# * dis#
				c1 = (nx# * lvx# + ny# * lvy# + nz# * lvz#) * 256
				c2 = (rnx# * lvx# + rny# * lvy# + rnz# * lvz#) ^ 10 * 256
				argb = s\argb
				z# = dis2#
			EndIf
		EndIf
	Next
	
	If Shadowed(ix#, iy#, iz#) Then
		c1 = ShadowDarkness
		c2 = 0
	Else
		If c1 < ShadowDarkness Then c1 = ShadowDarkness
		If c2 < 0 Then c2 = 0
	EndIf
	
	c3 = c1 * c Shr 8
	r = (argb And $FF0000) * c3 Shr 8 + c2 Shl 16
	g = (argb And $00FF00) * c3 Shr 8 + c2 Shl 8
	b = (argb And $0000FF) * c3 Shr 8 + c2
	
	If argb <> $000000 Then argb = SendRay(ix#, iy#, iz#, rnx#, rny#, rnz#, c - 32, recursive_times - 1)
	
	r = r + (argb And $FF0000)
	g = g + (argb And $00FF00)
	b = b + (argb And $0000FF)
	
	If r > $FF0000 Then r = $FF0000 Else r = r And $FF0000
	If g > $00FF00 Then g = $00FF00 Else g = g And $00FF00
	If b > $0000FF Then b = $0000FF Else b = b And $0000FF
	Return r Or g Or b
End Function

Function Shadowed(x#, y#, z#)
	evx# = lightx# - x#
	evy# = lighty# - y#
	evz# = lightz# - z#
	dis# = Sqr(evx# * evx# + evy# * evy# + evz# * evz#)
	evx# = evx# / dis#
	evy# = evy# / dis#
	evz# = evz# / dis#
	For s.Sphere = Each Sphere
		svx# = s\x# - x#
		svy# = s\y# - y#
		svz# = s\z# - z#
		dis2# = svx# * evx# + svy# * evy# + svz# * evz#
		dis# = svx# * svx# + svy# * svy# + svz# * svz# - dis2# * dis2#
		If dis2# > 0 And dis# < s\rad# * s\rad# Then Return True
	Next
End Function

Function SetCameraPosition(x#, y#, z#)
	camerax# = x#
	cameray# = y#
	cameraz# = z#
End Function

Function SetLightPosition(x#, y#, z#)
	lightx# = x#
	lighty# = y#
	lightz# = z#
End Function

Function SetAmbientLight(col)
	ambientcol = col
End Function

Function AddEntity(typ$, p1$ = "", p2$ = "", p3$ = "", p4$ = "", p5$ = "")
	Select Lower(typ$)
		Case "sphere"
			s.Sphere = New Sphere
			s\x# = p1$
			s\y# = p2$
			s\z# = p3$
			s\rad# = p4$
			s\argb = p5$
		Case "plane"
			p.Plane = New Plane
			p\ny# = p1$
			p\dis# = p2$
			p\argb = p3$
		Default
			RuntimeError "Entity type not found."
	End Select
End Function

Function SetupNormals()
	width = GraphicsWidth()
	height = GraphicsHeight()
	nz# = 200
	
	For y = 0 To height - 1
		ny# = Float(height Shr 1 - y) * 240.0 / Float(width)
		
		For x = 0 To width - 1
			nx# = Float(width Shr 1 - x) * 240.0 / Float(width)
			dis# = Sqr(nx# * nx# + ny# * ny# + nz# * nz#)
			normalx#(x, y) = -nx# / dis#
			normaly#(x, y) = ny# / dis#
			normalz#(x, y) = nz# / dis#
		Next
		
	Next
End Function
