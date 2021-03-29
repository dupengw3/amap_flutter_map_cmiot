import 'package:amap_flutter_map/amap_flutter_map.dart';
import 'package:amap_flutter_map_example/base_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../../const_config.dart';

class GeocoderDemoPage extends BasePage {
  GeocoderDemoPage(String title, String subTitle) : super(title, subTitle);

  @override
  Widget build(BuildContext context) {
    return _Body();
  }
}

class _Body extends StatefulWidget {
  const _Body();

  @override
  State<StatefulWidget> createState() => _State();
}

class _State extends State<_Body> {
  final TextEditingController nameController = TextEditingController();
  String address;

  AMapController controller;

  void _onMapCreated(AMapController controller) {
    this.controller = controller;
  }

  @override
  void initState() {
    nameController.text = '武侯区';
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final AMapWidget map = AMapWidget(
        apiKey: ConstConfig.amapApiKeys, onMapCreated: _onMapCreated,onGeocodeSearchError: (e){
            print(e);
        },
        );
    return Stack(
      children: [
        map,
        Container(
          height: 180,
          color: Colors.white,
          child: ListView(
            children: [
              TextFormField(
                style: TextStyle(fontSize: 15),
                controller: nameController,
                //keyboardType: TextInputType.text,
                //onChanged: controller.onUsernameChanged,
                decoration: InputDecoration(
                  contentPadding: EdgeInsets.fromLTRB(0, 0, 0, 0),
                  fillColor: Colors.white,
                  hintText: '请输入用地址',
                  enabledBorder: UnderlineInputBorder(
                    borderSide: BorderSide(
                      color: Color(0xAACCCCCC),
                    ),
                  ),
                  focusedBorder: UnderlineInputBorder(
                    borderSide: BorderSide(
                      color: Color(0xFF33BFE7),
                    ),
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () async {
                  String addr = nameController.text.trim();

                  await controller.goecodeSearch(addr,city: "北京").then((value) {
                    print('value  = $value');
                     setState(() {
                      address = "${value.latitude} - ${value.longitude}" ;
                    });
                    controller.moveCamera(CameraUpdate.newLatLng(value), animated: true);
                
                  });
                },
                child: Text('查询'),
              ),
              Text(address ?? '暂无地址')
            ],
          ),
        )
      ],
    );
  }
}
