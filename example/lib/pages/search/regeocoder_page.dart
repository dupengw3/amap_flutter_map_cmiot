import 'package:amap_flutter_map_example/base_page.dart';
import 'package:amap_flutter_map_example/widgets/amap_switch_button.dart';
import 'package:amap_flutter_map_example/widgets/bottons.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'package:amap_flutter_map/amap_flutter_map.dart';
import 'package:amap_flutter_base/amap_flutter_base.dart';

import '../../const_config.dart';

class ReGeocoderDemoPage extends BasePage {
    ReGeocoderDemoPage(String title, String subTitle) : super(title, subTitle);
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
        nameController.text = '[30.641982, 104.043390]';
        super.initState();
    }

    @override
    Widget build(BuildContext context) {
        final AMapWidget map = AMapWidget(
            apiKey: ConstConfig.amapApiKeys, onMapCreated: _onMapCreated);
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
                                    LatLng loc = LatLng(30.641982, 104.043390);

                                    await controller.reGoecodeSearch(loc).then((value) {
                                        print('value  = $value');
                                        setState(() {
                                            address = "$value" ;
                                        });
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

