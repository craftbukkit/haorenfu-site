import { NextResponse } from 'next/server';
export async function GET(){return NextResponse.json({online:true,playersOnline:null,playersMax:null,version:'Minecraft 1.21.1 / NeoForge',address:'haorenfu.cn',whitelist:true,message:'Minecraft好人服 1.21.1'})}
