const API='/api';
let itemCache=null;

// Keep prototype authentication inside the current browser tab. window.name is
// owned by the tab and survives same-origin navigation and refreshes. A duplicated
// tab may begin with a copy, but later changes remain local to that tab.
const nativeSessionStorage=window.sessionStorage;
const tabSessionKeys=['ecoUser','ecoMode','ecoRequest','ecoPickup','ecoReturnTo','ecoReturnMode','ecoOtpChallenge','ecoOtpEmail','ecoOtpExpiresAt','ecoPendingMode','ecoPendingReturnTo'];
const tabSessionPrefix='ecopickup-session:';

function readTabSession(){
  if(!window.name.startsWith(tabSessionPrefix))return {};
  try{return JSON.parse(window.name.slice(tabSessionPrefix.length))}catch{return {}}
}
function writeTabSession(values){window.name=`${tabSessionPrefix}${JSON.stringify(values)}`}
function startFreshTabSession(){writeTabSession({})}

const sessionStorage={
  getItem(key){const value=readTabSession()[key];return value===undefined?null:value},
  setItem(key,value){const values=readTabSession();values[key]=String(value);writeTabSession(values)},
  removeItem(key){const values=readTabSession();delete values[key];writeTabSession(values)}
};

async function initializeTabSession(){
  let values=readTabSession();
  const legacyTabId=nativeSessionStorage.getItem('ecoTabId');
  if(!Object.keys(values).length&&legacyTabId){
    try{values=JSON.parse(localStorage.getItem(`ecoTabSession:${legacyTabId}`)||'{}')}catch{values={}}
  }
  tabSessionKeys.forEach(key=>{const legacy=nativeSessionStorage.getItem(key);if(values[key]===undefined&&legacy!==null)values[key]=legacy;nativeSessionStorage.removeItem(key)});
  nativeSessionStorage.removeItem('ecoTabId');
  writeTabSession(values);
}

async function api(path,options={}){
  const headers={...(options.headers||{})};if(!(options.body instanceof FormData))headers['Content-Type']='application/json';
  const response=await fetch(`${API}${path}`,{...options,headers});
  const body=await response.json().catch(()=>({}));
  if(!response.ok) throw new Error(body.message||'Unable to complete the request');
  return body;
}

function currentUser(){
  try{
    const user=JSON.parse(sessionStorage.getItem('ecoUser')||'null');
    if(!user||user.id==null||typeof user.name!=='string'||!user.name.trim()||typeof user.email!=='string')return null;
    return user;
  }catch{return null}
}
function currentMode(){const saved=sessionStorage.getItem('ecoMode');if(saved==='BUYER'||saved==='SELLER')return saved;return currentUser()?.userType==='BUYER'?'BUYER':'SELLER'}
function money(value){return `₹${Number(value||0).toLocaleString('en-IN')}`}
function displayCondition(value){return ({WORKING:'Working',PARTIALLY_WORKING:'Partially Working',NOT_WORKING:'Not Working'})[value]||value}
function apiCondition(value){return value.toUpperCase().replaceAll(' ','_')}
function categoryIcon(category){return ({Mobile:'📱',Computers:'💻',Printers:'🖨️',TV:'🖥️',Accessories:'🔌',Appliances:'📻',Scrap:'⚙️'})[category]||'♻️'}
function initials(name='User'){return name.split(/\s+/).slice(0,2).map(part=>part[0]).join('').toUpperCase()}
function readable(value=''){return value.replaceAll('_',' ').toLowerCase().replace(/\b\w/g,letter=>letter.toUpperCase())}
function dateText(value){if(!value)return 'Not available';return new Intl.DateTimeFormat('en-IN',{day:'numeric',month:'long',year:'numeric'}).format(new Date(`${value}T00:00:00`))}
function showToast(message){const el=document.createElement('div');el.className='toast';el.textContent=message;document.body.append(el);setTimeout(()=>el.remove(),3500)}
function normalizeItem(item){return {...item,condition:displayCondition(item.condition),price:item.expectedPrice,icon:categoryIcon(item.category),date:item.createdAt?new Intl.DateTimeFormat('en-IN',{day:'numeric',month:'short'}).format(new Date(item.createdAt)):'Recently'}}
async function getItems(force=false){if(!itemCache||force)itemCache=(await api('/items')).map(normalizeItem);return itemCache}
function statusBadge(status){const special=status==='PENDING'||status==='REQUESTED'?' pending':status==='REJECTED'?' rejected':'';return `<span class="status-badge${special}">${readable(status)}</span>`}

function setupSessionNavigation(){
  const user=currentUser();
  const mode=currentMode();
  const dashboardUrl=mode==='BUYER'?'buyer-dashboard.html':'seller-dashboard.html';
  const navs=document.querySelectorAll('.main-nav');
  if(user){
    document.querySelectorAll('a[href="login.html"],a[href="signup.html"]').forEach(link=>link.remove());
    document.querySelectorAll('a[href="seller-dashboard.html"],a[href="buyer-dashboard.html"]').forEach(link=>{link.href=dashboardUrl;if(link.closest('.main-nav'))link.textContent='Dashboard'});
    navs.forEach(nav=>{
      if(!nav.querySelector(`a[href="${dashboardUrl}"]`))nav.insertAdjacentHTML('beforeend',`<a href="${dashboardUrl}">Dashboard</a>`);
      if(!nav.querySelector('[data-switch-mode]'))nav.insertAdjacentHTML('beforeend',`<button class="mode-switch" data-switch-mode>${mode==='BUYER'?'Switch to Selling':'Switch to Buying'}</button>`);
      if(!nav.querySelector('[data-logout]'))nav.insertAdjacentHTML('beforeend',`<button class="nav-logout" data-logout title="Logged in as ${user.name}">Logout</button>`);
    });
    if(!navs.length){
      const header=document.querySelector('.site-header');
      if(header&&!header.querySelector('[data-switch-mode]'))header.insertAdjacentHTML('beforeend',`<div class="account-controls"><button class="mode-switch" data-switch-mode>${mode==='BUYER'?'Switch to Selling':'Switch to Buying'}</button><button class="nav-logout" data-logout title="Logged in as ${user.name}">Logout</button></div>`);
    }
    document.querySelectorAll('[data-switch-mode]').forEach(button=>button.addEventListener('click',()=>{const next=currentMode()==='BUYER'?'SELLER':'BUYER';sessionStorage.setItem('ecoMode',next);location.href=next==='BUYER'?'buyer-dashboard.html':'seller-dashboard.html'}));
    document.querySelectorAll('[data-logout]').forEach(button=>button.addEventListener('click',()=>{startFreshTabSession();location.href='index.html'}));
  }else{
    document.querySelectorAll('a[href="seller-dashboard.html"],a[href="buyer-dashboard.html"]').forEach(link=>{if(link.closest('.main-nav'))link.style.display='none'});
  }
  document.querySelectorAll('a[href="add-item.html"]').forEach(link=>link.addEventListener('click',event=>{
    if(!user){event.preventDefault();sessionStorage.setItem('ecoReturnTo','add-item.html');sessionStorage.setItem('ecoReturnMode','SELLER');showToast('Please log in to list an item');setTimeout(()=>location.href='login.html',900);return}
    if(mode!=='SELLER'){event.preventDefault();showToast('Switch to Selling mode to list an item')}
  }));
}

function itemCard(item){
  const conditionClass=item.condition==='Working'?'':item.condition==='Partially Working'?'partial':'not-working';
  const visual=item.imageUrl?`<img src="${item.imageUrl}" alt="${item.name}">`:`<span>${item.icon}</span>`;
  return `<article class="product-card"><a class="product-image" href="item-details.html?id=${item.id}"><span class="condition ${conditionClass}">${item.condition}</span>${visual}</a><div class="product-body"><span class="product-category">${item.category}</span><h3><a href="item-details.html?id=${item.id}">${item.name}</a></h3><div class="product-meta"><span>📍 ${item.location}</span><span>${item.date}</span></div><div class="product-price">${money(item.price)}</div><a class="text-link" href="item-details.html?id=${item.id}">View details →</a></div></article>`;
}

async function hydrateHome(){
  const featured=document.querySelector('[data-featured-items]');
  if(!featured)return;
  try{
    const [items,stats]=await Promise.all([getItems(),api('/dashboard/admin')]);
    featured.innerHTML=items.length?items.slice(0,4).map(itemCard).join(''):'<div class="empty-state">No marketplace listings yet.</div>';
    document.querySelector('[data-home-listings]').textContent=stats.totalListings;
    document.querySelector('[data-home-users]').textContent=stats.totalUsers;
    document.querySelectorAll('.category-card').forEach(card=>{
      const category=new URL(card.href).searchParams.get('category');
      const count=items.filter(item=>item.category===category).length;
      const text=card.querySelector('p');if(text)text.textContent=`${count} listing${count===1?'':'s'}`;
    });
  }catch(error){featured.innerHTML='<div class="empty-state">Marketplace data is unavailable. Please start the Spring Boot server.</div>';showToast(error.message)}
}

async function hydrateMarketplace(){
  const market=document.querySelector('[data-market-items]');if(!market)return;
  try{
    const user=currentUser();const all=(await getItems()).filter(item=>!(user&&currentMode()==='BUYER'&&Number(item.seller?.id)===Number(user.id)));const search=document.querySelector('#market-search');const category=document.querySelector('#category-filter');const condition=document.querySelector('#condition-filter');const sort=document.querySelector('#sort-filter');
    const requestedCategory=new URLSearchParams(location.search).get('category');if(requestedCategory)category.value=requestedCategory;
    const render=()=>{let items=[...all];const term=search.value.trim().toLowerCase();if(term)items=items.filter(item=>`${item.name} ${item.category} ${item.location}`.toLowerCase().includes(term));if(category.value)items=items.filter(item=>item.category===category.value);if(condition.value)items=items.filter(item=>item.condition===condition.value);if(sort.value==='low')items.sort((a,b)=>a.price-b.price);if(sort.value==='high')items.sort((a,b)=>b.price-a.price);market.innerHTML=items.length?items.map(itemCard).join(''):'<div class="empty-state">No items match your filters.</div>';document.querySelector('[data-result-count]').textContent=`${items.length} item${items.length===1?'':'s'} available`};
    [search,category,condition,sort].forEach(element=>element.addEventListener(element===search?'input':'change',render));render();
  }catch(error){market.innerHTML='<div class="empty-state">Could not load items from the backend.</div>';showToast(error.message)}
}

async function hydrateItemDetails(){
  const detail=document.querySelector('[data-item-detail]');if(!detail)return;
  const id=new URLSearchParams(location.search).get('id');if(!id){detail.innerHTML='<div class="empty-state">No item was selected.</div>';return}
  try{
    const raw=await api(`/items/${id}`);const item=normalizeItem(raw);const seller=item.seller;const user=currentUser();const mode=currentMode();const ownListing=user&&Number(user.id)===Number(seller.id);const available=item.status==='ACTIVE'||item.status==='REQUESTED';
    let itemAction='<div class="notice unavailable-notice">This item is no longer available because it has already been reserved, collected, or completed.</div>';
    if(available&&!user)itemAction='<a class="button button-full" href="login.html">Login to Send Request</a>';
    if(available&&user&&mode==='BUYER'&&!ownListing)itemAction='<button class="button button-full" data-send-request>Send Request / I\'m Interested</button>';
    if(available&&user&&mode==='BUYER'&&ownListing)itemAction='<div class="notice">This is your own listing, so you cannot send a request for it.</div><a class="button button-secondary button-full" href="my-listings.html">View My Listings</a>';
    if(available&&user&&mode==='SELLER')itemAction='<div class="notice">You are currently in Selling mode. Switch to Buying mode when you want to request another seller\'s item.</div><button class="button button-secondary button-full" data-detail-switch>Switch to Buying</button>';
    const detailVisual=item.imageUrl?`<img src="${item.imageUrl}" alt="${item.name}">`:item.icon;
    detail.innerHTML=`<div class="detail-image">${detailVisual}</div><section class="detail-info"><span class="eyebrow">${item.category}</span><h1>${item.name}</h1><span class="condition">${item.condition}</span><div class="detail-price">${money(item.price)} <small style="font-size:12px;color:var(--muted);font-weight:500">expected value</small></div><p>${item.description}</p><div class="detail-meta"><div><small>Brand</small><strong>${item.brand||'Not specified'}</strong></div><div><small>Quantity</small><strong>${item.quantity}</strong></div><div><small>Location</small><strong>${item.location}</strong></div><div><small>Listed</small><strong>${item.date}</strong></div></div><div class="seller-box"><div class="avatar">${initials(seller.name)}</div><div><strong>${seller.name}</strong><small>Seller · ${seller.location}</small></div></div><div class="notice">Final value is determined after physical inspection. Pickup is free.</div>${itemAction}<a class="button button-secondary button-full" style="margin-top:10px" href="marketplace.html">Back to marketplace</a></section>`;
    const requestButton=document.querySelector('[data-send-request]');
    if(requestButton)requestButton.addEventListener('click',async()=>{try{const request=await api('/requests',{method:'POST',body:JSON.stringify({itemId:Number(item.id),buyerId:user.id})});sessionStorage.setItem('ecoRequest',JSON.stringify(request));showToast('Request sent to the seller');setTimeout(()=>location.href='requests.html',700)}catch(error){showToast(error.message)}});
    const detailSwitch=document.querySelector('[data-detail-switch]');if(detailSwitch)detailSwitch.addEventListener('click',()=>{sessionStorage.setItem('ecoMode','BUYER');location.reload()});
  }catch(error){detail.innerHTML='<div class="empty-state">This item could not be loaded.</div>';showToast(error.message)}
}

async function hydrateDashboard(){
  const page=location.pathname.split('/').pop();if(page!=='seller-dashboard.html'&&page!=='buyer-dashboard.html')return;
  const user=currentUser();if(!user){location.href='login.html';return}
  document.querySelector('.sidebar-user strong').textContent=user.name;document.querySelector('.sidebar-user small').textContent=user.email;
  const heading=document.querySelector('.dashboard-head h1');const cards=document.querySelectorAll('.stat-card strong');const body=document.querySelector('.data-table tbody');
  try{
    if(page==='seller-dashboard.html'){
      const badge=document.querySelector('.site-header .status-badge');if(badge)badge.textContent='Selling mode';
      sessionStorage.setItem('ecoMode','SELLER');heading.textContent=`Welcome back, ${user.name.split(' ')[0]} 👋`;
      const [items,requests]=await Promise.all([api(`/items/seller/${user.id}`),api(`/requests/seller/${user.id}`)]);const values=[items.length,items.filter(i=>i.status==='ACTIVE'||i.status==='REQUESTED').length,requests.filter(r=>r.status==='ACCEPTED'||r.status==='PICKUP_SCHEDULED').length,items.filter(i=>i.status==='COMPLETED').length];values.forEach((value,index)=>cards[index].textContent=value);
      body.innerHTML=items.length?items.slice(0,5).map(item=>`<tr><td>${categoryIcon(item.category)} ${item.name}</td><td>${money(item.expectedPrice)}</td><td>${statusBadge(item.status)}</td><td>${requests.filter(request=>request.item.id===item.id).length}</td></tr>`).join(''):'<tr><td colspan="4"><div class="empty-state">You have no listings yet. List your first item to get started.</div></td></tr>';
    }else{
      const badge=document.querySelector('.site-header .status-badge');if(badge)badge.textContent='Buying mode';
      sessionStorage.setItem('ecoMode','BUYER');heading.textContent=`Hello, ${user.name} ♻️`;const requests=await api(`/requests/buyer/${user.id}`);[requests.length,requests.filter(r=>r.status==='ACCEPTED').length,requests.filter(r=>r.status==='PICKUP_SCHEDULED').length,requests.filter(r=>r.status==='COMPLETED').length].forEach((value,index)=>cards[index].textContent=value);body.innerHTML=requests.length?requests.slice(0,5).map(request=>`<tr><td>RE${String(request.id).padStart(3,'0')}</td><td>${request.item.name}</td><td>${request.seller.name}</td><td>${statusBadge(request.status)}</td></tr>`).join(''):'<tr><td colspan="4"><div class="empty-state">You have not requested any items yet.</div></td></tr>';
    }
  }catch(error){showToast(`Could not load dashboard: ${error.message}`)}
}

async function hydrateMyListings(){
  if(location.pathname.split('/').pop()!=='my-listings.html')return;const user=currentUser();if(!user){location.href='login.html';return}sessionStorage.setItem('ecoMode','SELLER');
  try{const items=await api(`/items/seller/${user.id}`);const body=document.querySelector('#listing-rows');body.innerHTML=items.length?items.map(item=>`<tr><td>${categoryIcon(item.category)} ${item.name}</td><td>${item.category}</td><td>${money(item.expectedPrice)}</td><td>${statusBadge(item.status)}</td><td><a class="text-link" href="item-details.html?id=${item.id}">View</a></td></tr>`).join(''):'<tr><td colspan="5"><div class="empty-state">No listings yet. Add your first item to the marketplace.</div></td></tr>'}catch(error){showToast(error.message)}
}

async function hydrateRequests(){
  if(location.pathname.split('/').pop()!=='requests.html')return;const user=currentUser();if(!user){location.href='login.html';return}
  const heading=document.querySelector('.page-hero h1'),description=document.querySelector('.page-hero p'),title=document.querySelector('.panel-heading h2'),counter=document.querySelector('[data-request-count]'),head=document.querySelector('.data-table thead'),body=document.querySelector('.data-table tbody'),links=document.querySelectorAll('[data-role-dashboard]');
  try{
    if(currentMode()==='BUYER'){
      links.forEach(link=>{link.href='buyer-dashboard.html';link.textContent='Buyer Dashboard'});heading.textContent='My requests';description.textContent='Track the requests you have sent to sellers.';title.textContent='Requests sent';const requests=await api(`/requests/buyer/${user.id}`);counter.textContent=`${requests.length} total`;counter.className='status-badge';head.innerHTML='<tr><th>Request ID</th><th>Item</th><th>Seller</th><th>Expected Value</th><th>Status</th><th>Action</th></tr>';body.innerHTML=requests.length?requests.map(r=>{const action=['PICKUP_SCHEDULED','COMPLETED'].includes(r.status)?`<a class="text-link" href="track-pickup.html?request=${r.id}">Track pickup →</a>`:'<span style="color:var(--muted)">Waiting</span>';return `<tr><td>RE${String(r.id).padStart(3,'0')}</td><td>${categoryIcon(r.item.category)} ${r.item.name}</td><td>${r.seller.name}</td><td>${money(r.item.expectedPrice)}</td><td>${statusBadge(r.status)}</td><td>${action}</td></tr>`}).join(''):'<tr><td colspan="6"><div class="empty-state">You have not sent any requests yet.</div></td></tr>';return;
    }
    links.forEach(link=>{link.href='seller-dashboard.html';link.textContent='Seller Dashboard'});heading.textContent='Incoming requests';description.textContent='Review buyer interest in your listed items.';title.textContent='Buyer requests';const requests=await api(`/requests/seller/${user.id}`);const pending=requests.filter(r=>r.status==='PENDING').length;counter.textContent=`${pending} need${pending===1?'s':''} action`;counter.className=`status-badge${pending?' pending':''}`;head.innerHTML='<tr><th>Request ID</th><th>Item</th><th>Buyer</th><th>Expected Value</th><th>Status</th><th>Action</th></tr>';body.innerHTML=requests.length?requests.map(r=>{let action='<span style="color:var(--muted)">No action</span>';if(r.status==='PENDING')action=`<button class="button button-small" data-request-action="ACCEPTED" data-id="${r.id}">Accept</button> <button class="button button-secondary button-small" data-request-action="REJECTED" data-id="${r.id}">Reject</button>`;if(r.status==='ACCEPTED')action=`<a class="text-link" href="schedule-pickup.html" data-schedule="${r.id}">Schedule pickup →</a>`;if(['PICKUP_SCHEDULED','COMPLETED'].includes(r.status))action=`<a class="text-link" href="track-pickup.html?request=${r.id}">Track pickup →</a>`;return `<tr><td>RE${String(r.id).padStart(3,'0')}</td><td>${categoryIcon(r.item.category)} ${r.item.name}</td><td>${r.buyer.name}</td><td>${money(r.item.expectedPrice)}</td><td>${statusBadge(r.status)}</td><td>${action}</td></tr>`}).join(''):'<tr><td colspan="6"><div class="empty-state">No buyers have requested your items yet.</div></td></tr>';
    document.querySelectorAll('[data-request-action]').forEach(button=>button.addEventListener('click',async()=>{try{const updated=await api(`/requests/${button.dataset.id}/status`,{method:'PATCH',body:JSON.stringify({status:button.dataset.requestAction})});sessionStorage.setItem('ecoRequest',JSON.stringify(updated));await hydrateRequests()}catch(error){showToast(error.message)}}));document.querySelectorAll('[data-schedule]').forEach(link=>link.addEventListener('click',()=>sessionStorage.setItem('ecoRequest',JSON.stringify(requests.find(r=>String(r.id)===link.dataset.schedule)))));
  }catch(error){showToast(error.message)}
}

async function setupAddItem(){
  const form=document.querySelector('#add-item-form');if(!form)return;const user=currentUser();if(!user){sessionStorage.setItem('ecoReturnTo','add-item.html');sessionStorage.setItem('ecoReturnMode','SELLER');showToast('Please log in to list an item');setTimeout(()=>location.href='login.html',900);return}sessionStorage.setItem('ecoMode','SELLER');form.elements.location.value=user.location||'';
  const imageInput=form.querySelector('#photos');const preview=form.querySelector('[data-photo-preview]');const uploadText=form.querySelector('[data-upload-text]');
  imageInput.addEventListener('change',()=>{const file=imageInput.files[0];if(!file){preview.hidden=true;uploadText.textContent='Click to upload a primary item photo';return}if(file.size>5*1024*1024){imageInput.value='';showToast('Item photo must be 5 MB or smaller');return}preview.src=URL.createObjectURL(file);preview.hidden=false;uploadText.textContent=file.name});
  form.addEventListener('submit',async event=>{event.preventDefault();const button=form.querySelector('button[type=submit]');button.disabled=true;const data=new FormData(form);const item={sellerId:user.id,name:data.get('name'),category:data.get('category'),brand:data.get('brand'),condition:apiCondition(data.get('condition')),quantity:Number(data.get('quantity')),description:data.get('description'),expectedPrice:Number(data.get('price')),location:data.get('location')};const payload=new FormData();payload.append('item',new Blob([JSON.stringify(item)],{type:'application/json'}));if(imageInput.files[0])payload.append('image',imageInput.files[0]);try{await api('/items',{method:'POST',body:payload});location.href='my-listings.html'}catch(error){showToast(error.message);button.disabled=false}});
}

function setupAuth(){
  document.querySelectorAll('[data-demo-login]').forEach(form=>form.addEventListener('submit',async event=>{
    event.preventDefault();const button=form.querySelector('button[type=submit]');button.disabled=true;
    try{
      const email=form.elements.email.value;const password=form.elements.password.value;
      let mode='SELLER';
      if(form.dataset.auth==='signup'){
        mode=form.querySelector('[name=role]:checked').value.toUpperCase();
        await api('/auth/signup',{method:'POST',body:JSON.stringify({name:form.elements.name.value,email,phone:form.elements.phone.value,password,location:form.elements.location.value,userType:'BOTH'})});
      }
      const challenge=await api('/auth/login',{method:'POST',body:JSON.stringify({email,password})});
      const returnTo=sessionStorage.getItem('ecoReturnTo');const returnMode=sessionStorage.getItem('ecoReturnMode');
      startFreshTabSession();
      sessionStorage.setItem('ecoOtpChallenge',challenge.challengeId);
      sessionStorage.setItem('ecoOtpEmail',challenge.maskedEmail);
      sessionStorage.setItem('ecoOtpExpiresAt',String(Date.now()+challenge.expiresInSeconds*1000));
      sessionStorage.setItem('ecoPendingMode',returnMode||mode);
      if(returnTo)sessionStorage.setItem('ecoPendingReturnTo',returnTo);
      location.href='verify-otp.html';
    }catch(error){showToast(error.message);button.disabled=false}
  }));
}

function setupOtp(){
  const form=document.querySelector('#otp-form');if(!form)return;
  const challengeId=sessionStorage.getItem('ecoOtpChallenge');
  if(!challengeId){location.href='login.html';return}
  document.querySelector('[data-otp-email]').textContent=sessionStorage.getItem('ecoOtpEmail')||'your email address';
  const countdown=document.querySelector('[data-otp-countdown]');const expiresAt=Number(sessionStorage.getItem('ecoOtpExpiresAt'));
  const updateCountdown=()=>{const seconds=Math.max(0,Math.ceil((expiresAt-Date.now())/1000));countdown.textContent=seconds?`${Math.floor(seconds/60)}:${String(seconds%60).padStart(2,'0')}`:'Expired';if(!seconds)form.querySelector('button[type=submit]').disabled=true};
  updateCountdown();const timer=setInterval(updateCountdown,1000);
  form.addEventListener('submit',async event=>{
    event.preventDefault();const button=form.querySelector('button[type=submit]');button.disabled=true;
    try{
      const mode=sessionStorage.getItem('ecoPendingMode')||'SELLER';const returnTo=sessionStorage.getItem('ecoPendingReturnTo');
      const user=await api('/auth/verify-otp',{method:'POST',body:JSON.stringify({challengeId,code:form.elements.code.value})});
      clearInterval(timer);startFreshTabSession();sessionStorage.setItem('ecoUser',JSON.stringify(user));sessionStorage.setItem('ecoMode',mode);
      location.href=returnTo||`${mode==='BUYER'?'buyer':'seller'}-dashboard.html`;
    }catch(error){showToast(error.message);button.disabled=false;form.elements.code.select()}
  });
}

async function setupSchedule(){
  const form=document.querySelector('#schedule-form');if(!form)return;const user=currentUser();let request=JSON.parse(sessionStorage.getItem('ecoRequest')||'null');if(!user){location.href='login.html';return}
  try{const mode=currentMode();if(!request||request.status!=='ACCEPTED'){const requests=await api(`/${mode==='BUYER'?'requests/buyer':'requests/seller'}/${user.id}`);request=requests.find(entry=>entry.status==='ACCEPTED')}if(!request)throw new Error('No accepted request is available for pickup');sessionStorage.setItem('ecoRequest',JSON.stringify(request));form.elements.fullName.value=user.name;form.elements.phone.value=user.phone;form.elements.city.value=user.location;const tomorrow=new Date();tomorrow.setDate(tomorrow.getDate()+1);const dateValue=tomorrow.toISOString().slice(0,10);form.elements.date.min=dateValue;form.elements.date.value=dateValue;document.querySelector('[data-pickup-item]').textContent=`${request.item.name} × ${request.item.quantity}`;document.querySelector('[data-pickup-request]').textContent=`RE${String(request.id).padStart(3,'0')}`;document.querySelector('[data-pickup-party]').textContent=mode==='SELLER'?request.buyer.name:request.seller.name}catch(error){form.querySelector('button[type=submit]').disabled=true;showToast(error.message)}
  form.addEventListener('submit',async event=>{event.preventDefault();const data=Object.fromEntries(new FormData(form));try{const pickup=await api('/pickups',{method:'POST',body:JSON.stringify({requestId:request.id,address:`${data.house}, ${data.building}, ${data.area}, ${data.city} - ${data.pincode}`,pickupDate:data.date,timeSlot:data.time,contactNumber:data.phone,instructions:data.instructions})});sessionStorage.setItem('ecoPickup',JSON.stringify(pickup));location.href='confirmation.html'}catch(error){showToast(error.message)}});
}

async function hydrateConfirmation(){
  if(location.pathname.split('/').pop()!=='confirmation.html')return;const saved=JSON.parse(sessionStorage.getItem('ecoPickup')||'null');const card=document.querySelector('[data-confirmation]');if(!saved){card.innerHTML='<div class="empty-state">No newly scheduled pickup was found.</div>';document.querySelector('[data-confirm-pickup]').disabled=true;return}try{const pickup=await api(`/pickups/${saved.id}`);const item=pickup.request.item;document.querySelector('[data-confirm-item]').textContent=`${item.name} × ${item.quantity}`;document.querySelector('[data-confirm-condition]').textContent=displayCondition(item.condition);document.querySelector('[data-confirm-address]').textContent=pickup.address;document.querySelector('[data-confirm-date]').textContent=dateText(pickup.pickupDate);document.querySelector('[data-confirm-time]').textContent=pickup.timeSlot;document.querySelector('[data-confirm-value]').textContent=`${money(item.expectedPrice)}*`;document.querySelector('[data-confirm-pickup]').onclick=()=>location.href=`track-pickup.html?id=${pickup.id}`}catch(error){showToast(error.message)}
}

async function hydrateTracking(){
  if(location.pathname.split('/').pop()!=='track-pickup.html')return;
  const user=currentUser();const params=new URLSearchParams(location.search);const requestId=params.get('request');let id=params.get('id')||JSON.parse(sessionStorage.getItem('ecoPickup')||'null')?.id;
  try{
    if(!id&&requestId)id=(await api(`/pickups/request/${requestId}`)).id;
    if(!id&&user){const pickups=await api(`/pickups/user/${user.id}`);id=pickups[0]?.id}
    if(!id)throw new Error('No pickup is available to track');
    const [pickup,reward]=await Promise.all([api(`/pickups/${id}`),api(`/rewards/pickup/${id}`)]);const item=pickup.request.item;
    document.querySelector('[data-track-id]').textContent=`#${pickup.id}`;document.querySelector('[data-track-item]').textContent=item.name;document.querySelector('[data-track-date]').textContent=dateText(pickup.pickupDate);document.querySelector('[data-track-time]').textContent=pickup.timeSlot;document.querySelector('[data-track-status]').innerHTML=statusBadge(pickup.status);document.querySelector('[data-track-party]').textContent=`${pickup.request.buyer.name} and ${pickup.request.seller.name}`;
    document.querySelector('[data-reward-estimated]').textContent=money(reward.estimatedValue);document.querySelector('[data-reward-final]').textContent=reward.finalApprovedValue==null?'Pending inspection':money(reward.finalApprovedValue);document.querySelector('[data-reward-method]').textContent=reward.paymentMethod?readable(reward.paymentMethod):'Not selected';document.querySelector('[data-reward-status]').innerHTML=statusBadge(reward.paymentStatus);
    const order=['REQUEST_SUBMITTED','REQUEST_ACCEPTED','PICKUP_SCHEDULED','COLLECTOR_ASSIGNED','PICKED_UP','PROCESSING_COMPLETED'];const current=order.indexOf(pickup.status);document.querySelectorAll('[data-stage]').forEach((stage,index)=>{stage.classList.toggle('done',index<=current);stage.classList.toggle('current',index===current);stage.querySelector('.timeline-dot').textContent=index<=current?'✓':'○'});const collector=document.querySelector('[data-collector]');collector.textContent=pickup.collectorName?`${pickup.collectorName} is assigned to this pickup.`:'A collector has not been assigned yet.';
    const action=document.querySelector('[data-transaction-action]');const isBuyer=user&&Number(user.id)===Number(pickup.request.buyer.id);const isSeller=user&&Number(user.id)===Number(pickup.request.seller.id);
    if(!user)action.innerHTML='<div class="notice">Log in to manage this pickup.</div>';
    else if(isBuyer&&pickup.status==='PICKUP_SCHEDULED'){
      action.innerHTML=`<h2>Buyer/recycler action: assign collection</h2><p class="form-help">Enter the person or organization that will collect the item from the seller.</p><form data-assign-collector><div class="form-group"><label>Collector name</label><input class="form-control" name="collectorName" value="${user.name} Collection Team" required></div><button class="button button-full" type="submit">Assign Collector →</button></form>`;
      action.querySelector('form').addEventListener('submit',async event=>{event.preventDefault();const button=event.currentTarget.querySelector('button');button.disabled=true;try{await api(`/pickups/${id}/status`,{method:'PATCH',body:JSON.stringify({status:'COLLECTOR_ASSIGNED',collectorName:event.currentTarget.elements.collectorName.value})});showToast('Collector assigned');await hydrateTracking()}catch(error){showToast(error.message);button.disabled=false}});
    }else if(isBuyer&&pickup.status==='COLLECTOR_ASSIGNED'){
      action.innerHTML=`<h2>Buyer/recycler action: complete handover</h2><p class="form-help">After inspection, enter the agreed value and confirm how the seller was paid.</p><form data-complete-handover><div class="form-row"><div class="form-group"><label>Final paid value (₹)</label><input class="form-control" name="finalValue" type="number" min="0" step="0.01" value="${Number(reward.estimatedValue)}" required></div><div class="form-group"><label>Payment method</label><select class="form-control" name="paymentMethod" required><option value="UPI">UPI</option><option value="CASH">Cash</option><option value="BANK_TRANSFER">Bank Transfer</option></select></div></div><div class="notice">Confirm only after the item has been collected and the seller has been paid.</div><button class="button button-full" type="submit">Confirm Pickup & Payment →</button></form>`;
      action.querySelector('form').addEventListener('submit',async event=>{event.preventDefault();const button=event.currentTarget.querySelector('button');button.disabled=true;try{await api(`/pickups/${id}/handover`,{method:'POST',body:JSON.stringify({finalApprovedValue:Number(event.currentTarget.elements.finalValue.value),paymentMethod:event.currentTarget.elements.paymentMethod.value})});showToast('Pickup and payment recorded');await hydrateTracking()}catch(error){showToast(error.message);button.disabled=false}});
    }else if(isBuyer&&pickup.status==='PICKED_UP'){
      action.innerHTML='<h2>Buyer/recycler action: finish processing</h2><p class="form-help">Use this after the collected item has entered reuse, refurbishment or responsible recycling.</p><button class="button button-full" data-complete-processing>Mark Processing Completed →</button>';
      action.querySelector('button').addEventListener('click',async event=>{event.currentTarget.disabled=true;try{await api(`/pickups/${id}/status`,{method:'PATCH',body:JSON.stringify({status:'PROCESSING_COMPLETED'})});showToast('Transaction completed');await hydrateTracking()}catch(error){showToast(error.message);event.currentTarget.disabled=false}});
    }else if(isSeller)action.innerHTML=`<h2>Seller view</h2><div class="notice">The buyer/recycler controls collection updates. This page automatically shows the latest pickup and payment record.</div>`;
    else if(pickup.status==='PROCESSING_COMPLETED')action.innerHTML='<h2>Transaction complete</h2><div class="notice">The item was collected, payment was recorded and processing was completed.</div>';
    else action.innerHTML='<div class="notice">No action is available for this account at the current stage.</div>';
  }catch(error){document.querySelector('[data-tracking]').innerHTML=`<div class="empty-state">${error.message}</div>`}
}

async function hydrateAdmin(){
  if(location.pathname.split('/').pop()!=='admin-dashboard.html')return;try{const [stats,pickups]=await Promise.all([api('/dashboard/admin'),api('/pickups')]);const cards=document.querySelectorAll('.stat-card strong');[stats.totalUsers,stats.totalListings,stats.pendingRequests,stats.completedPickups].forEach((value,index)=>cards[index].textContent=value);const body=document.querySelector('.data-table tbody');body.innerHTML=pickups.length?pickups.slice(-5).reverse().map(p=>`<tr><td>${p.id}</td><td>${p.request.item.name}</td><td>${p.request.seller.name}</td><td>${statusBadge(p.status)}</td></tr>`).join(''):'<tr><td colspan="4"><div class="empty-state">No pickups have been scheduled yet.</div></td></tr>'}catch(error){showToast(error.message)}
}

document.addEventListener('DOMContentLoaded',async()=>{
  await initializeTabSession();
  const menu=document.querySelector('.menu-toggle');if(menu)menu.addEventListener('click',()=>document.querySelector('.main-nav').classList.toggle('open'));
  const page=location.pathname.split('/').pop();
  if(currentUser()&&['seller-dashboard.html','add-item.html','my-listings.html'].includes(page))sessionStorage.setItem('ecoMode','SELLER');
  if(currentUser()&&page==='buyer-dashboard.html')sessionStorage.setItem('ecoMode','BUYER');
  setupSessionNavigation();
  setupAuth();
  setupOtp();
  await Promise.all([hydrateHome(),hydrateMarketplace(),hydrateItemDetails(),hydrateDashboard(),hydrateMyListings(),hydrateRequests(),setupAddItem(),setupSchedule(),hydrateConfirmation(),hydrateTracking(),hydrateAdmin()]);
});
