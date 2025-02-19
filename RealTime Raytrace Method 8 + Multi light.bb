;ChooseScreenMode()
Graphics3D 640,480,32,2

Global camerax#, cameray#, cameraz#
Global ambientcol

;Raytracer
InitRaytracer()
SetCameraPosition(0, -25, -120)
SetAmbientLight(200)
CreateScene()

;Loadscreen
Local Px#=camerax#
Local Py#=cameray#
Local Pz#=cameraz#

Local Speed#=10


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
	
	For s.sphere=Each Sphere		
		
		s\anim#=s\anim#+s\ampl#
		
		s\y# = -20+Sin(s\anim#)*s\grav# ; -5 +Cos(i*10) * 20;(Cos(r) * 10) * (Sin(i) * 10)
		
	Next
	
	Raytrace() : Flip False
Wend

End

Function CreateScene()
	AddEntity("Light", 35, 200, -135)                  ;Light
	AddEntity("Light", -35, 200, -135)                  ;Light
	AddEntity("Plane", 1, 60, $303030)               ;Floor
	
	
	
	Colorize%=Rand($FF9955) : x#=0 : y#=-30 : z#=0 : r#=25
	AddEntity("Sphere", x# ,y# ,z# , r# , Colorize%) 
	
	
	
	
	For i=0 To 160 Step 20
		
		
		x# = Sin(i*10) * 50
		y# = -5
		z# = Cos(i*10) * 50
		
		Colorize%=Rand($FF9955)  r#=Rnd(1,13)
		AddEntity("Sphere", x# ,y# ,z# , r# , Colorize%) 
		
	Next
	
End Function

Type Light
	Field x#, y#, z#
End Type

Type Plane
	Field nx#, ny#, nz#
	Field dis#
	Field argb
End Type

Type Sphere
	Field x#, y#, z#
	Field rad#
	Field argb
	
	Field grav#
	Field ampl#
	Field anim#
End Type

Dim normalx#(-1, -1)
Dim normaly#(-1, -1)
Dim normalz#(-1, -1)



Function InitRaytracer()
	w = GraphicsWidth()
	h = GraphicsHeight()
	
	Dim normalx#(w, h)
	Dim normaly#(w, h)
	Dim normalz#(w, h)
	
	SetupNormals()
End Function

Function Raytrace()
	
	Cls : LockBuffer(BackBuffer())
	width = GraphicsWidth()
	height = GraphicsHeight()
	
	For y = 0 To height - 1  Step 1
		For x = 0 To width - 1 Step 1
			rgb = SendRay(camerax#, cameray#, cameraz#, normalx#(x, y), normaly#(x, y), normalz#(x, y), ambientcol,10)
			
			If rgb <>$000000 Then 
				WritePixelFast x, y, rgb, BackBuffer()
			EndIf
		Next
	Next
	
	
	
	UnlockBuffer(BackBuffer())
End Function

Function SendRay(ex#, ey#, ez#, evx#, evy#, evz#, c = 255, recursive_times =18)
If recursive_times < 0 Then Return
z# = 10000

l.Light = First Light

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
		
		lvx# = l\x# - ix#
		lvy# = l\y# - iy#
		lvz# = l\z# - iz#
		
		dis# = Sqr(lvx# * lvx# + lvy# * lvy# + lvz# * lvz#)
		
		lvx# = lvx# / dis#
		lvy# = lvy# / dis#
		lvz# = lvz# / dis#
		
		dis# = (nx# * evx# + ny# * evy# + nz# * evz#) *2
		
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
			lvx# = l\x# - ix#
			lvy# = l\y# - iy#
			lvz# = l\z# - iz#
			dis# = Sqr(lvx# * lvx# + lvy# * lvy# + lvz# * lvz#)
			lvx# = lvx# / dis#
			lvy# = lvy# / dis#
			lvz# = lvz# / dis#
			dis# = (nx# * evx# + ny# * evy# + nz# * evz#) * 2
			rnx# = evx# - nx# * dis#
			rny# = evy# - ny# * dis#
			rnz# = evz# - nz# * dis#
			c1 = (nx# * lvx# + ny# * lvy# + nz# * lvz#) * 256
			c2 = c2 + (rnx# * lvx# + rny# * lvy# + rnz# * lvz#) ^ 10 * 256
			argb = s\argb
			z# = dis2#
		EndIf
	EndIf
Next

	sh = Shadowed(ix#, iy#, iz#)
	
	If sh < 255 Then
		c1 = (255 - 10000.0 / Float(sh)) * .4
		c2 = 0
	Else
		If c1 < 30 Then c1 = 30
		If c2 < 0 Then c2 = 0
	EndIf
	
	
	
	c3 = c1 * c Shr 8
	r = (argb And $FF0000) * c3 Shr 8 + c2 Shl 16
	g = (argb And $00FF00) * c3 Shr 8 + c2 Shl 8
	b = (argb And $0000FF) * c3 Shr 8 + c2
	
	If argb <> 0 Then argb = SendRay(ix#, iy#, iz#, rnx#, rny#, rnz#, c, recursive_times - 1)
	
	r = r + (argb And $FF0000)
	g = g + (argb And $00FF00)
	b = b + (argb And $0000FF)
	
	If r >= $FF0000 Then r = $FF0000 Else r = r And $FF0000
	
	If g >= $00FF00 Then g = $00FF00 Else g = g And $00FF00
	
	If b >= $0000FF Then b = $0000FF Else b = b And $0000FF
	
	Return r Or g Or b 
End Function

Function Shadowed(x#, y#, z#)
	col = 255
	
For l.Light = Each Light
	evx# = l\x# - x#
	evy# = l\y# - y#
	evz# = l\z# - z#
	
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
		
		If dis2# > 0 And dis# < s\rad# * s\rad# Then col = col / 2
	Next
Next


Return col 
End Function

Function SetCameraPosition(x#, y#, z#)
camerax# = x#
cameray# = y#
cameraz# = z#
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
		
		s\ampl#=Rnd(-2,2)
		s\grav#=Rnd(-20,20)
		
		
	Case "plane"
		p.Plane = New Plane
		p\ny# = p1$
		p\dis# = p2$
		p\argb = p3$
		
	Case "light"
		l.Light = New Light
		l\x# = p1$
		l\y# = p2$
		l\z# = p3$
		
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



Type tMode
	
	Field Width%
	Field Height%
	Field Depth%
	Field Mode%
	Field Id%
	
End Type

Function ChooseScreenMode(Title$="My App",MinWidth=0)
	
	Graphics3D 400,240,0,2
	AppTitle Title$
	
	Local Count%=CountGfxModes3D()
	
	Local Total%
	Local Current%=0
	
	Local Width%
	Local Height%
	Local Depth%
	Local Mode%
	
	
	Local C%
	Local Y%
	Local O%
	
	Local Sx%=GraphicsWidth()
	Local Sy%=GraphicsHeight()
	Local ScrollY#=50
	
	Local Size%=20	
	Local Font01=LoadFont("arial",Size%-2,0,0,0)
	Local Font02=LoadFont("arial",Size%-2,1,0,0)
	
	
	For i=1 To Count%
		
		If GfxModeWidth(I)>=MinWidth
			
			Current%=Current%+1
			
			m.tMode=New tMode
			m\Width%=GfxModeWidth(I)
			m\Height%=GfxModeHeight(I)
			m\Depth%=GfxModeDepth(I)
			m\Mode%=2
			m\Id%=Current%
			
			Total%=Total%+1
			
		EndIf
	Next	
	
	Current=1
	
	Repeat
		
		; ---
		; back
		; ---
		Viewport 0, 0, Sx%,Sy%
		
		Color 0,52,69
		Rect 0,0,Sx%,Sy%,1
		
		ScrollY#=ScrollY#+0.7 : If ScrollY#>Sy%-50 Then ScrollY#=50
		
		Color 0,56,73 : Line 0,ScrollY#+0,Sx%,ScrollY#+0
		Color 0,60,77 : Line 0,ScrollY#+1,Sx%,ScrollY#+1
		Color 0,64,81 : Line 0,ScrollY#+2,Sx%,ScrollY#+2
		Color 0,68,85 : Line 0,ScrollY#+3,Sx%,ScrollY#+3
		Color 5,79,96 : Line 0,ScrollY#+4,Sx%,ScrollY#+4
		Color 0,68,85 : Line 0,ScrollY#+5,Sx%,ScrollY#+5
		Color 0,64,81 : Line 0,ScrollY#+6,Sx%,ScrollY#+6
		
		Color 0,29,52
		Rect 0,0,Sx%,50,1
		Rect 0,Sy%-50,Sx%,50,1
		
		SetFont Font01
		
		Color 0,69,112
		Text Sx%/2,Sy%-25,"Press <up><down> / <lef><right>",True,True
		
		; ---
		; Bar
		; ---
		Color 0,128,64
		Rect 0,110,Sx%,Size%,1
		
		Color 0,255,0
		Rect 0,110,Sx%,Size%,0
		
		Viewport 0, 60, Sx%, Sy%-120
		
		; -------
		; Refresh
		; -------
		C%=0
		Y%=101
		
		For d.tMode =Each tMode
			
			C%=C%+Size%
			
			If d\Id%=Current% Then	
				
			;	Color 255,128,0
				Color 100,255,100
				SetFont Font02
				
				
				If KeyHit(203) Or KeyHit(205) Or MouseHit(2) d\Mode%=3-(d\Mode)
					
					
				Else
					
			;	Color 168,54,0
					Color C%/1.2 Mod 255,128,0
					SetFont Font01
					
				EndIf
				
				Text 70,Y%+C%-O%,d\Width%,True,True
				Text 105,Y%+C%-O%,"/",True,True
				Text 140,Y%+C%-O%,d\Height%,True,True
				
				Text 190,Y%+C%-O%,d\Depth%,True,True
				Text 220,Y%+C%-O%,"Bits",True,True
				
				Select d\Mode%
					Case 1
						Caption$="Screen"
					Case 2
						Caption$="Windowed"
				End Select
				
				Text 300,Y%+C%-O%,Caption$,True,True
			Next
			
			Msz=MouseZSpeed()
			
			If KeyHit(200)  Or Msz>0 Then 
				If Current%>1 Then
					O%=O%-Size%
					
					Current%=Current%-1
				EndIf
			EndIf
			
			If KeyHit(208) Or Msz<0 Then 
				If Current%<Total% Then
					O%=O%+Size%
					
					Current%=Current%+1
				EndIf
			EndIf
			
			
			
			If KeyHit(28) Or MouseHit(1) Then
				For e.tMode =Each tMode
					If e\Id=Current% Then
						
						Graphics3D e\Width%,e\Height%,e\Depth%,e\Mode%
						Return True
						
					EndIf
				Next
			EndIf		
			
			Flip
		Forever
		
End Function
;~IDEal Editor Parameters:
;~F#15C#166
;~C#Blitz3D