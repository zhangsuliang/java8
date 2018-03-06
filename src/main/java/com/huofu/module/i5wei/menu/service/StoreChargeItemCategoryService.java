package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemCategoryDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeSubitemDAO;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemCategory;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreChargeItemCategoryDTO;
import huofucore.facade.i5wei.menu.StoreChargeItemDTO;
import huofucore.facade.i5wei.menu.StoreProductDTO;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收费项目分类
 * Created by lixuwei on 16/3/21.
 */
@Service
public class StoreChargeItemCategoryService {

    @Autowired
    private StoreChargeItemCategoryDAO storeChargeItemCategoryDAO;

    @Autowired
    private StoreProductDAO storeProductDAO;

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    /**
     * 添加一个收费项目分类
     * <p>
     * create by lixuwei on 16/3/21
     *
     * @param merchantId
     * @param storeId
     * @param name
     * @return
     */
    public StoreChargeItemCategory addChargeItemCategory(int merchantId, long storeId, String name) throws T5weiException, TException {

        List<StoreChargeItemCategory> storeChargeItemCategories = storeChargeItemCategoryDAO.getStoreChargeItemCategorys(merchantId, storeId, null, true, true);

        StoreChargeItemCategory storeChargeItemCategory = new StoreChargeItemCategory();

        if (storeChargeItemCategory.canAdd(name, storeChargeItemCategories)) {
            storeChargeItemCategory.setMerchantId(merchantId);
            storeChargeItemCategory.setStoreId(storeId);
            storeChargeItemCategory.setName(name);
            storeChargeItemCategory.setCreateTime(System.currentTimeMillis());
            storeChargeItemCategoryDAO.create(storeChargeItemCategory);

        } else {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
                    "storeId[" + storeId + "] store chargeItemCategory name[" + name + "] already exists");
        }

        return storeChargeItemCategory;
    }

    /**
     * 修改一个 收费项目分类
     * <p>
     * create by lixuwei on 16/3/21
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @return
     */
    public StoreChargeItemCategory updateChargeItemCategory(int merchantId, long storeId, int categoryId, String name) throws T5weiException, TException {

        StoreChargeItemCategory storeChargeItemCategory = storeChargeItemCategoryDAO.getById(merchantId, storeId, categoryId, true, true);
        List<StoreChargeItemCategory> storeChargeItemCategories = storeChargeItemCategoryDAO.getStoreChargeItemCategorys(merchantId, storeId, null, true, true);

        if (storeChargeItemCategory.canAdd(name, storeChargeItemCategories)) {
            storeChargeItemCategory.setName(name);
            storeChargeItemCategory.setUpdateTime(System.currentTimeMillis());

            storeChargeItemCategoryDAO.update(storeChargeItemCategory);
        } else {

            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
                    "storeId[" + storeId + "] store chargeItemCategory name[" + name + "] already exists");
        }

        return storeChargeItemCategory;
    }


    /**
     * 显示当前店铺的菜品分类集合
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreChargeItemCategory> getStoreChargeItemCategorys(int merchantId, long storeId) {

        return storeChargeItemCategoryDAO.getStoreChargeItemCategorys(merchantId, storeId, null, true, true);
    }

    /**
     * 根据分类名称，获取当前店铺的菜品分类
     *
     * @param merchantId
     * @param storeId    param name
     * @return
     */
    public StoreChargeItemCategory getStoreChargeItemCategoryByName(int merchantId, long storeId, String name) {
        return storeChargeItemCategoryDAO.getStoreChargeItemCategoryByName(merchantId, storeId, name);
    }

    /**
     * 新增或修改 收费项目分类
     *
     * @param merchantId
     * @param storeId
     * @param categoryId 新增时categoryId为0
     * @param name
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreChargeItemCategory saveChargeItemCategory(int merchantId, long storeId, int categoryId, String name) throws T5weiException, TException {

        if (categoryId == 0) {
            return addChargeItemCategory(merchantId, storeId, name);
        } else {
            return updateChargeItemCategory(merchantId, storeId, categoryId, name);
        }
    }

    /**
     * @param merchantId
     * @param storeId
     * @param categoryIds
     */
    public List<StoreChargeItemCategory> getCategoryByIds(int merchantId, long storeId, Set<Integer> categoryIds) {
        List<StoreChargeItemCategory> storeChargeItemCategorys = Lists.newArrayList();
        if (categoryIds.contains(0)) {
            //新建一个默认 '未分类' 的分类
            StoreChargeItemCategory storeChargeItemCategory = new StoreChargeItemCategory();
            storeChargeItemCategory.setMerchantId(merchantId);
            storeChargeItemCategory.setStoreId(storeId);
            storeChargeItemCategory.setCategoryId(0);
            storeChargeItemCategory.setName("未分类");

            storeChargeItemCategorys.add(storeChargeItemCategory);
        }

        List<StoreChargeItemCategory> categorysByIds = storeChargeItemCategoryDAO.getCategoryByIds(merchantId, storeId, categoryIds);
        storeChargeItemCategorys.addAll(categorysByIds);
        return storeChargeItemCategorys;
    }

    /**
     * @param merchantId
     * @param storeId
     * @param categoryIds
     * @param loadProducts
     * @param loadChargeItems
     * @param loadDeleted
     */
    public List<StoreChargeItemCategoryDTO> getCategories(int merchantId, long storeId, List<Integer> categoryIds, boolean loadProducts, boolean loadChargeItems, boolean loadDeleted) {

        //新建一个默认 '未分类' 的分类
        StoreChargeItemCategory storeChargeItemCategory = new StoreChargeItemCategory();
        storeChargeItemCategory.setMerchantId(merchantId);
        storeChargeItemCategory.setStoreId(storeId);
        storeChargeItemCategory.setCategoryId(0);
        storeChargeItemCategory.setName("未分类");

        List<StoreChargeItemCategory> storeChargeItemCategories;
        storeChargeItemCategories = storeChargeItemCategoryDAO.getStoreChargeItemCategorys(merchantId, storeId, categoryIds, true, true);
        if (categoryIds == null || categoryIds.isEmpty()) {
            storeChargeItemCategories.add(storeChargeItemCategory);
        } else {
            if (categoryIds.contains(0)) {
                storeChargeItemCategories.add(storeChargeItemCategory);
            }
        }

        List<Integer> queryCategoryIds = storeChargeItemCategories.stream().map(StoreChargeItemCategory::getCategoryId).collect(Collectors.toList());
        ArrayListMultimap<Long, StoreChargeSubitem> storeChargeSubitemMap = ArrayListMultimap.create();
        List<StoreChargeItem> storeChargeItems = Lists.newArrayList();
        List<StoreProduct> storeProducts = Lists.newArrayList();
        if (loadChargeItems) {
            storeChargeItems = storeChargeItemDAO.getChargeItemByCategoryIds(merchantId, storeId, queryCategoryIds, loadDeleted);
            //加载收费子项
            List<Long> queryChargeItemIds = storeChargeItems.stream().map(StoreChargeItem::getChargeItemId).collect(Collectors.toList());
            List<StoreChargeSubitem> StoreChargeSubitems = storeChargeSubitemDAO.getListByChargeItemIds(merchantId, storeId, queryChargeItemIds);
            for (StoreChargeSubitem storeChargeSubitem : StoreChargeSubitems) {
                storeChargeSubitemMap.put(storeChargeSubitem.getChargeItemId(), storeChargeSubitem);
            }
        }
        if (loadProducts) {
            storeProducts = storeProductDAO.getProductByCategoryIds(merchantId, storeId, queryCategoryIds, loadDeleted);
        }
        return buildCategoryDTO(storeChargeItemCategories, storeChargeItems, storeChargeSubitemMap, storeProducts);
    }

    /**
     * 组装收费项目分类dto
     *
     * @param storeChargeItemCategorys
     * @param storeChargeItems
     * @return
     */
    private List<StoreChargeItemCategoryDTO> buildCategoryDTO(List<StoreChargeItemCategory> storeChargeItemCategorys,
                                                              List<StoreChargeItem> storeChargeItems,
                                                              ArrayListMultimap<Long, StoreChargeSubitem> storeChargeSubitemMap,
                                                              List<StoreProduct> storeProducts) {

        List<StoreChargeItemCategoryDTO> dtos = Lists.newArrayList();

        ArrayListMultimap<Integer, StoreChargeItem> chargeItemMap = ArrayListMultimap.create();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            chargeItemMap.put(storeChargeItem.getCategoryId(), storeChargeItem);
        }

        ArrayListMultimap<Integer, StoreProduct> productMap = ArrayListMultimap.create();
        for (StoreProduct storeProduct : storeProducts) {
            productMap.put(storeProduct.getCategoryId(), storeProduct);
        }

        for (StoreChargeItemCategory category : storeChargeItemCategorys) {
            StoreChargeItemCategoryDTO dto = new StoreChargeItemCategoryDTO();
            BeanUtil.copy(category, dto);
            Integer categoryId = category.getCategoryId();
            Set<Long> filterProductIds = Sets.newHashSet();
            List<StoreChargeItem> storeChargeItemsOfCategory = chargeItemMap.get(categoryId);
            if (storeChargeItemsOfCategory != null && storeChargeItemsOfCategory.size() > 0) {
                List<StoreChargeItemDTO> chargeItemDTOs = Lists.newArrayList();
                for (StoreChargeItem storeChargeItem : storeChargeItemsOfCategory) {
                    StoreChargeItemDTO chargeItemDTO = new StoreChargeItemDTO();
                    BeanUtil.copy(storeChargeItem, chargeItemDTO);
                    chargeItemDTOs.add(chargeItemDTO);

                    //添加需要过滤的产品Id集合
                    List<StoreChargeSubitem> subitems = storeChargeSubitemMap.get(storeChargeItem.getChargeItemId());
                    if (subitems != null && subitems.size() == 1) {
                        filterProductIds.add(subitems.get(0).getProductId());
                    }
                }
                //添加收费项目集合到分类
                dto.setChargeItems(chargeItemDTOs);
            } else {
                dto.setChargeItems(Lists.newArrayList());
            }

            List<StoreProduct> storeProductsOfCategory = productMap.get(categoryId);
            if (storeProductsOfCategory != null && storeProductsOfCategory.size() > 0) {
                List<StoreProductDTO> productDTOs = Lists.newArrayList();
                for (StoreProduct storeProduct : storeProductsOfCategory) {
                    if (!filterProductIds.contains(storeProduct.getProductId())) {
                        StoreProductDTO storeProductDTO = new StoreProductDTO();
                        BeanUtil.copy(storeProduct, storeProductDTO);
                        productDTOs.add(storeProductDTO);
                    }
                }
                //添加产品集合到分类
                dto.setProducts(productDTOs);
            } else {
                dto.setProducts(Lists.newArrayList());
            }
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 更新产品或收费项目的分类
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @param productIds
     * @param chargeItemIds
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateChargeItemAndProductCategory(int merchantId, long storeId, int categoryId, List<Long> productIds, List<Long> chargeItemIds) {
        //根据productIds 得到相关的产品和收费项目
        //根据chargeItemIds 得到相关的收费项目和产品
        List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds);
        List<Long> updateProductIds = products.stream().map(StoreProduct::getProductId).collect(Collectors.toList());
        List<Long> queryChargeItemIds = storeChargeSubitemDAO.getOnlyChargeItemIdsContainProductIds(merchantId, storeId, updateProductIds);
        List<StoreChargeItem> updateStoreChargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, queryChargeItemIds, false, true);
        List<StoreChargeItem> chargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, false, true);
        List<Long> updateChargeItemIds = chargeItems.stream().map(StoreChargeItem::getChargeItemId).collect(Collectors.toList());
        List<Long> onlyChargeItemIds = storeChargeSubitemDAO.getChargeItemIdsByOnlyChargeItem(merchantId, storeId, updateChargeItemIds);
        //要关联更新的菜品
        List<Long> queryProductIds = storeChargeSubitemDAO.getProductIdsInChargeItemIds(merchantId, storeId, onlyChargeItemIds);
        //找到同一个菜品的关联的单点收费项目
        List<Long> glChargeItemIds = storeChargeSubitemDAO.getOnlyChargeItemIdsContainProductIds(merchantId, storeId, queryProductIds);
        List<StoreChargeItem> glChargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, glChargeItemIds, false, true);
        List<StoreProduct> updateProducts = storeProductDAO.getListInIds(merchantId, storeId, queryProductIds);

        //取要更新的产品或收费项目的合集
        List<StoreProduct> updateCategoryProducts = mergeProducts(products, updateProductIds, updateProducts, categoryId);
        List<StoreChargeItem> updateCategoryChargeItems = mergeChargeItems(chargeItems, glChargeItems, updateStoreChargeItems, categoryId);

        //批量更新产品的分类
        storeProductDAO.batchUpdateCategory(merchantId, storeId, updateCategoryProducts);
        //批量更新收费项目的分类
        storeChargeItemDAO.batchUpdateCategory(merchantId, storeId, updateCategoryChargeItems);

    }

    private List<StoreChargeItem> mergeChargeItems(List<StoreChargeItem> chargeItems, List<StoreChargeItem> glChargeItems, List<StoreChargeItem> updateStoreChargeItems, int categoryId) {
        List<StoreChargeItem> updateChargeItems = Lists.newArrayList();
        Map<Long, StoreChargeItem> chargeItemMap = Maps.newHashMap();
        for (StoreChargeItem chargeItem : chargeItems) {
            chargeItem.setCategoryId(categoryId);
            chargeItemMap.put(chargeItem.getChargeItemId(), chargeItem);
        }
        for (StoreChargeItem chargeItem : glChargeItems) {
            chargeItem.setCategoryId(categoryId);
            chargeItemMap.put(chargeItem.getChargeItemId(), chargeItem);
        }
        for (StoreChargeItem chargeItem : updateStoreChargeItems) {
            chargeItem.setCategoryId(categoryId);
            chargeItemMap.put(chargeItem.getChargeItemId(), chargeItem);
        }
        Collection<StoreChargeItem> values = chargeItemMap.values();
        updateChargeItems.addAll(values);
        return updateChargeItems;
    }

    private List<StoreProduct> mergeProducts(List<StoreProduct> products, List<Long> updateProductIds, List<StoreProduct> productsByChargeItem, int categoryId) {
        List<StoreProduct> updateProducts = Lists.newArrayList();
        for (StoreProduct product : products) {
            product.setCategoryId(categoryId);
            updateProducts.add(product);
        }

        for (StoreProduct product : productsByChargeItem) {
            long productId = product.getProductId();
            if (!updateProductIds.contains(productId)) {
                product.setCategoryId(categoryId);
                updateProducts.add(product);
            }
        }
        return updateProducts;
    }

    /**
     * 判断收费项目是否存在该店铺内
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @return
     */
    public boolean checkStoreCategoryExist(int merchantId, long storeId, int categoryId) {
        StoreChargeItemCategory category = storeChargeItemCategoryDAO.getById(merchantId, storeId, categoryId, false, false);
        if (category != null) {
            //note 虽然有些多余,为了正确
            if (category.getCategoryId() == categoryId) {
                return true;
            }
        }
        return false;
    }
}
