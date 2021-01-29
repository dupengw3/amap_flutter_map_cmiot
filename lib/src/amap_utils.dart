part of amap_flutter_base;

class AMapUtil {
  static AMapUtil _instance;
  static double _devicePixelRatio;
  static void init(BuildContext context) {
    _instance ??= AMapUtil._();
    _devicePixelRatio = MediaQuery.of(context).devicePixelRatio;
  }

  AMapUtil._();

  factory AMapUtil() {
    assert(
      _instance != null,
      '\n请确认AMapUtil已经执行了初始化\n可通过AMapUtil.init(BuildContext)执行初始化',
    );
    return _instance;
  }

  /// 获取当前设备的屏幕像素比
  static double get devicePixelRatio => _devicePixelRatio;
}
