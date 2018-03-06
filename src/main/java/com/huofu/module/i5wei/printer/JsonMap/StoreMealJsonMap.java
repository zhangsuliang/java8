package com.huofu.module.i5wei.printer.JsonMap;

import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealProductDTO;
import huofuhelper.util.NumberUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/**
 * Created by jiajin.nervous on 16/10/25.
 */
public class StoreMealJsonMap {

    public static List<Map<String,Object>> toStoreMealMapList(List<StoreMealDTO> storeMealDTOs,Map<Long,StoreMealPort> storeMealPortMap){
        List<Map<String,Object>> list = new ArrayList<>();
        for(StoreMealDTO storeMealDTO : storeMealDTOs){
            Map<String,Object> map = toStoreMealMap(storeMealDTO,storeMealPortMap);
            if(!map.isEmpty()){
                list.add(map);
            }
        }
        return list;
    }

    public static Map<String,Object> toStoreMealMap(StoreMealDTO storeMealDTO,Map<Long,StoreMealPort> storeMealPortMap){
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> storeMealChargeMapList = toStoreMealChargeMapList(storeMealDTO.getStoreMealChargeDTOs());
        if(!storeMealChargeMapList.isEmpty()){
            map.put("packaged", NumberUtil.bool2Int(storeMealDTO.isPackaged()));
            map.put("take_serial_number",storeMealDTO.getTakeSerialNumber());
            map.put("take_serial_seq",storeMealDTO.getTakeSerialSeq());
            map.put("take_mode",storeMealDTO.getTakeMode());
            map.put("count",storeMealDTO.getCount());
            map.put("port_id",storeMealDTO.getPortId());
            if(storeMealPortMap.get(storeMealDTO.getPortId()) != null){
                map.put("port_name",storeMealPortMap.get(storeMealDTO.getPortId()).getName());
            }
            map.put("port_letter",storeMealDTO.getPortLetter());
            map.put("port_count",storeMealDTO.getPortCount());
            map.put("store_meal_charges",storeMealChargeMapList);
        }
       return map;
    }

    public static List<Map<String,Object>> toStoreMealChargeMapList(List<StoreMealChargeDTO> storeMealChargeDTOs){
        List<Map<String,Object>> list = new ArrayList<>();
        for(StoreMealChargeDTO storeMealChargeDTO : storeMealChargeDTOs){
            Map<String,Object> map = toStoreMealChargeMap(storeMealChargeDTO);
            if(!map.isEmpty()){
                list.add(map);
            }
        }
        return list;
    }

    public static Map<String,Object> toStoreMealChargeMap(StoreMealChargeDTO storeMealChargeDTO){
        Map<String,Object> map = new HashMap<>();
        //打印堂食的部分
        if(!storeMealChargeDTO.isPackaged()){
            map.put("charge_item_id",storeMealChargeDTO.getChargeItemId());
            map.put("charge_item_name",storeMealChargeDTO.getChargeItemName());
            map.put("amount",storeMealChargeDTO.getAmount());
            map.put("packaged",storeMealChargeDTO.isPackaged());
            map.put("show_products",storeMealChargeDTO.isShowProducts());
            map.put("refund_meal",storeMealChargeDTO.isRefundMeal());
            map.put("port_id",storeMealChargeDTO.getPortId());
            map.put("store_meal_products",toStoreMealProductMapList(storeMealChargeDTO.getStoreMealProductDTOs()));
        }
        return map;
    }

    public static List<Map<String,Object>> toStoreMealProductMapList(List<StoreMealProductDTO> storeMealProductDTOs){
        List<Map<String,Object>> list = new ArrayList<>();
        for(StoreMealProductDTO storeMealProductDTO : storeMealProductDTOs){
            list.add(toStoreMealProductMap(storeMealProductDTO));
        }
        return list;
    }

    public static Map<String,Object> toStoreMealProductMap(StoreMealProductDTO storeMealProductDTO){
        Map<String,Object> map = new HashMap<>();
        map.put("charge_item_id",storeMealProductDTO.getChargeItemId());
        map.put("product_id",storeMealProductDTO.getProductId());
        map.put("product_name",storeMealProductDTO.getProductName());
        map.put("amount",storeMealProductDTO.getAmount());
        map.put("unit",storeMealProductDTO.getUnit());
        map.put("remark",storeMealProductDTO.getRemark());
        return map;
    }

}
