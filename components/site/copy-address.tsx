"use client";
import { useState } from "react";
import { Check, Copy } from "lucide-react";
import { server } from "@/lib/site-data";
export function CopyAddress({compact=false}:{compact?:boolean}){const[copied,setCopied]=useState(false);async function copy(){try{await navigator.clipboard.writeText(server.address);setCopied(true);setTimeout(()=>setCopied(false),1800)}catch{}}return <button type="button" onClick={copy} className={compact?"copy-address compact":"copy-address"}><span><small>JAVA 版服务器地址</small><strong>{server.address}</strong></span>{copied?<Check/>:<Copy/>}</button>}
