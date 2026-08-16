extends Node2D

var pos := Vector2(480, 410)
var heading := -PI / 2.0
var speed := 120.0
var lap := 0
var elapsed := 0.0
var last_y := pos.y
var steer := 0.0

func _ready():
    set_process(true)
    queue_redraw()

func _input(event):
    if event is InputEventScreenTouch or event is InputEventScreenDrag:
        if event.position.x < get_viewport_rect().size.x * 0.5:
            steer = -1.0
        else:
            steer = 1.0
        if event is InputEventScreenTouch and not event.pressed:
            steer = 0.0

func _process(delta):
    elapsed += delta
    var keyboard := Input.get_axis("ui_left", "ui_right")
    var turn := keyboard if abs(keyboard) > 0.01 else steer
    heading += turn * 2.25 * delta
    speed = clamp(speed + 8.0 * delta, 120.0, 260.0)
    pos += Vector2(cos(heading), sin(heading)) * speed * delta
    var bounds := Rect2(90, 70, 780, 400)
    pos.x = clamp(pos.x, bounds.position.x, bounds.end.x)
    pos.y = clamp(pos.y, bounds.position.y, bounds.end.y)
    if last_y < 270.0 and pos.y >= 270.0 and pos.x > 700.0:
        lap += 1
    last_y = pos.y
    queue_redraw()

func _draw():
    draw_rect(Rect2(45, 35, 870, 470), Color("20252a"), true)
    draw_rect(Rect2(100, 85, 760, 370), Color("090b0d"), true)
    draw_rect(Rect2(245, 175, 470, 190), Color("20252a"), true)
    draw_line(Vector2(710, 270), Vector2(860, 270), Color.WHITE, 8.0)
    draw_string(ThemeDB.fallback_font, Vector2(28, 28), "GHOSTKART · lap %d · %.1fs · tap left/right to steer" % [lap, elapsed], HORIZONTAL_ALIGNMENT_LEFT, -1, 20, Color("00ff41"))
    draw_set_transform(pos, heading)
    draw_rect(Rect2(-18, -10, 36, 20), Color("00ff41"), true)
    draw_rect(Rect2(6, -7, 14, 14), Color("d0ffd8"), true)
    draw_set_transform(Vector2.ZERO, 0.0)
