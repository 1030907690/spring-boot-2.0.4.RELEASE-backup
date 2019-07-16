package com.rw.article.common.utils.pay;




import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TreeUtils {

    /**
     * 部门树结构排序
     *
     * @param list 部门列表
     */
   /* public static void treeDeptSort(List<TbDept> list, int pid) {
        treeDeptSort(list, pid, 0);
    }

    private static void treeDeptSort(List<TbDept> list, int pid, int sort) {
        if (list != null && list.size() > 0) {
            for (TbDept dept : list) {
                if (Objects.equals(pid, dept.getPid())) {
                    treeDeptSort(list, dept.getId(), dept.getSort());
                    dept.setSort(sort * 100 + dept.getSort());
                }
            }
        }
    }

    *//**
     * 资源数结构排序和清理删除状态数据
     *
     * @param menuList 菜单list
     * @param parentId 父节点ID
     *//*
    public static void treeMenuHide(List<TbResource> menuList, Integer parentId) {
        treeMenuHide(menuList, parentId, 0, 0);
    }

    private static void treeMenuHide(List<TbResource> menuList, Integer parentId, Integer parentHide, Integer parentSort) {
        if (menuList != null && menuList.size() > 0) {
            for (TbResource menu : menuList) {
                if (Objects.equals(parentId, menu.getParentId())) {
                    menu.setIsHide(parentHide == 0 ? menu.getIsHide() : 1);
                    treeMenuHide(menuList, menu.getResId(), menu.getIsHide(), menu.getSort());
                    menu.setSort(parentSort * 100 + menu.getSort());
                }
            }
        }
    }

    *//**
     * 菜单树递归
     *
     * @param menuList 菜单列表
     * @param parentId 父菜单编号
     * @return 递归后得菜单列表
     *//*
    public static List<TbResource> treeMenuList(List<TbResource> menuList, Integer parentId) {
        List<TbResource> childMenu = new ArrayList<TbResource>();
        if (menuList != null && menuList.size() > 0) {
            for (TbResource menu : menuList) {
                Integer menuId = menu.getResId();
                Integer pid = menu.getParentId();
                if (Objects.equals(parentId, pid)) {
                    List<TbResource> node = treeMenuList(menuList, menuId);
                    menu.setChildren(node);
                    childMenu.add(menu);
                }
            }
        }
        childMenu.sort(Comparator.comparingInt(TbResource::getSort));
        return childMenu;
    }*/
}
