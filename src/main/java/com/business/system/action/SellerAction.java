package com.business.system.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.business.system.bean.SellerBean;
import com.business.system.bean.UserBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import net.sf.rose.initial.BootStart;
import net.sf.rose.jdbc.PageBean;
import net.sf.rose.jdbc.dao.BeanDAO;
import net.sf.rose.jdbc.query.BeanSQL;
import net.sf.rose.jdbc.service.Service;
import net.sf.rose.util.StringUtil;
import net.sf.rose.web.utils.WebUtils;

/** 
 * @author fengjian E-mail: 9110530@qq.com 
 * @version 创建时间：2017年2月17日 下午3:58:56 
 * 类说明：商家管理
 */

@Controller
@RequestMapping("/seller")
public class SellerAction {
	private static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * 商家信息列表
	 */
	@ResponseBody
	@RequestMapping("/list.do")
	public List<SellerBean> list(HttpServletRequest request, Service service) {
		PageBean page = WebUtils.getPageBean(request);
		Map<String, Object> map = WebUtils.getRequestData(request);
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.setEntityClass(SellerBean.class);
		query.createSql(map);
		query.setPage(page);
		return dao.list();
	}
	
	/**
	 * 商家信息列表(不分页)
	 */
	@ResponseBody
	@RequestMapping("/totalList.do")
	public List<SellerBean> totalList(HttpServletRequest request, Service service) {
		Map<String, Object> map = WebUtils.getRequestData(request);
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.setEntityClass(SellerBean.class);
		query.createSql(map);
		return dao.list();
	}

	/**
	 * 获取商家信息
	 */
	@ResponseBody
	@RequestMapping("/load.do")
	public SellerBean load(Service service, String id) {
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.setEntityClass(SellerBean.class);
		query.createSql(id);
		return dao.load();
	}

	/**
	 * 保存商家信息
	 */
	@ResponseBody
	@RequestMapping("/save.do")
	public String save(HttpServletRequest request, Service service, String data) {
		SellerBean bean = StringUtil.parse(data, SellerBean.class);
		//获取客户端文件
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile client_file = multipartRequest.getFile("sellerImg");//上传控件

		if(client_file.getOriginalFilename() != null && !"".equals(client_file.getOriginalFilename()) 
				&& bean.getSellerImg() != null && !"".equals(bean.getSellerImg())){
			String ctxPath = "";
			if (OS.indexOf("linux") >= 0) {
				ctxPath = "/usr/local/app/appserver-01/webapps/imglibs";
			} else if (OS.indexOf("windows") >= 0) {
				ctxPath = BootStart.WINDOWS_PATH;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
			String ymd = sdf.format(new Date());
			ctxPath += File.separator + ymd + File.separator;
			
			// 创建文件夹
			File file = new File(ctxPath);
			if (!file.exists()) {
				file.mkdirs();
			}
			String fileName = null;
			UUID uuid = null;
			fileName = client_file.getOriginalFilename();// 获取原文件名
			fileName = fileName.substring(fileName.lastIndexOf("."));
			// System.out.println("filename="+UUID.randomUUID()+"."+fileName);
			uuid = UUID.randomUUID();
			File uploadFile = new File(ctxPath + uuid + fileName);
			try {
				FileCopyUtils.copy(client_file.getBytes(), uploadFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bean.setSellerImg(ymd + "/" + uuid + fileName);
		}
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.createSaveSql(bean,"status,strSeller");
		int res = dao.update();
		return String.valueOf(res);
	}
	
	/**
	 * 保存商家信息
	 */
	@ResponseBody
	@RequestMapping("/saveBase.do")
	public int saveBase( Service service, SellerBean bean) {
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.createSaveSql(bean,"status,strSeller");
		return dao.update();
	}

	/**
	 * 删除商家信息
	 */
	@ResponseBody
	@RequestMapping("/delete.do")
	public int delete(Service service, String id) {
		BeanDAO dao = new BeanDAO(service);
		BeanSQL query = dao.getQuerySQL();
		query.createDeleteSql(SellerBean.class, id);
		int res = dao.update();
		// 删除商家用户信息
		if (res >0) {
			query.createDeleteSql(UserBean.class, "sellerNo", id);
			dao.update();
		}
		return res;
	}
}
