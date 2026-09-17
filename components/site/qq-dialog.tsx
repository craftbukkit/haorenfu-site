"use client";
import { useState } from "react";
import { QrCode, X } from "lucide-react";
import { server } from "@/lib/site-data";
export function QqDialog({label="扫码加入 QQ 群"}:{label?:string}){const[open,setOpen]=useState(false);return <><button className="secondary-button" onClick={()=>setOpen(true)}><QrCode/> {label}</button>{open&&<div className="modal-backdrop" role="presentation" onMouseDown={()=>setOpen(false)}><div className="qq-dialog" role="dialog" aria-modal="true" aria-label="Minecraft 好人服 QQ 群" onMouseDown={e=>e.stopPropagation()}><button className="modal-close" onClick={()=>setOpen(false)} aria-label="关闭"><X/></button><h2>Minecraft 好人服 QQ 群</h2><p>群号 {server.group} · 扫码申请白名单并获取客户端公告</p><img className="qq-image" src="/images/qq-qrcode.jpg" alt={`Minecraft 好人服 QQ 群 ${server.group} 的二维码`}/></div></div>}</>}
