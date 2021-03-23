import 'package:flutter/material.dart';
// RaisedButton and ElevatedButton(升高的按钮:只有文字)
// FlatButton vs TextButton(文字的按钮:只有背景色和文字)
// OutlineButton vs OutlinedButton(边框按钮:只有边框和文字)

Widget textButton(
    {Key key,
    @required Widget child,
    @required Function onPressed,
    Color backgroundcolor,
    Color textColor,
    Color disabledBackgroundColor,
    Color disabledTextColor,
    double borderRadius,
    Color borderColor,
    double bordeWidth,
    }) {


  RoundedRectangleBorder shape = RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(
                        Radius.circular(borderRadius ?? 2),
                      ),
                      side: BorderSide(color: borderColor ?? Colors.transparent, width: bordeWidth ?? 0));

  return TextButton(
      key: key,
      style: ButtonStyle(
        foregroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) => states.contains(MaterialState.disabled)
              ? disabledTextColor
              : textColor,
        ),
        backgroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) =>
              states.contains(MaterialState.disabled) ? disabledBackgroundColor : backgroundcolor,
        ),
        shape: MaterialStateProperty.all(shape),
      ),
      onPressed: onPressed,
      child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8.0), child: child));
}



Widget elevatedButton(
    {Key key,
    @required Widget child,
    @required Function onPressed,
    Color backgroundcolor,
    Color textColor,
    Color disabledBackgroundColor,
    Color disabledTextColor,
    double borderRadius,
    Color borderColor,
    double bordeWidth,
    }) {


  RoundedRectangleBorder shape = RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(
                        Radius.circular(borderRadius ?? 2),
                      ),
                      side: BorderSide(color: borderColor ?? Colors.transparent, width: bordeWidth ?? 0));

  return ElevatedButton(
      key: key,
      style: ButtonStyle(
        foregroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) => states.contains(MaterialState.disabled)
              ? disabledTextColor
              : textColor,
        ),
        backgroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) =>
              states.contains(MaterialState.disabled) ? disabledBackgroundColor : backgroundcolor,
        ),
        shape: MaterialStateProperty.all(shape),
      ),
      onPressed: onPressed,
      child: child);
}


Widget outlinedButton(
    {Key key,
    @required Widget child,
    @required Function onPressed,
    Color backgroundcolor,
    Color textColor,
    Color disabledBackgroundColor,
    Color disabledTextColor,
    double borderRadius,
    Color borderColor,
    double bordeWidth,
    }) {


  RoundedRectangleBorder shape = RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(
                        Radius.circular(borderRadius ?? 2),
                      ),
                      side: BorderSide(color: borderColor ?? Colors.transparent, width: bordeWidth ?? 0));

  return OutlinedButton(
      key: key,
      style: ButtonStyle(
        foregroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) => states.contains(MaterialState.disabled)
              ? disabledTextColor
              : textColor,
        ),
        backgroundColor: MaterialStateProperty.resolveWith<Color>(
          (Set<MaterialState> states) =>
              states.contains(MaterialState.disabled) ? disabledBackgroundColor : backgroundcolor,
        ),
        shape: MaterialStateProperty.all(shape),
      ),
      onPressed: onPressed,
      child: child);
}
