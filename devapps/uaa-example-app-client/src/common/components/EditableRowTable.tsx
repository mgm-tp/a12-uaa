/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
import React, { Dispatch, SetStateAction } from "react";

import {
  Button,
  ButtonGroup,
  HintTooltip,
  Icon,
  MessageBox,
  Pagination,
  Select,
  TextAreaStateless,
  TextLineStateless,
  WarningTooltip,
} from "@com.mgmtp.a12.widgets/widgets-core";
import { generateUid } from "@com.mgmtp.a12.widgets/widgets-core/lib/common/index.js";
import {
  BaseColumnType,
  DefaultTableComponentRenderers,
  getDataByKey,
  Table,
  TableContextProvider,
  TableRenderPropsType,
  useTableContext,
} from "@com.mgmtp.a12.widgets/widgets-core/lib/table/new-api/index.js";

import { Key } from "ts-keycode-enum";

import {
  ConnectorLocator,
  RestRequestPayload,
  RestServerConnector,
} from "@com.mgmtp.a12.utils/utils-connector/lib/main/index.js";

const ACCEPT_JSON_HEADER = ["Accept", "application/json"];
const CONTENT_TYPE_JSON_HEADER = [
  "Content-Type",
  "application/json;charset=utf8",
];

type CompanyType = {
  id: string;
  name: string;
  countryCode: string;
  taxNumber: string;
};

const makeCancelable = (promise: Promise<unknown>) => {
  let hasCanceled_ = false;

  const wrappedPromise = new Promise((resolve, reject) => {
    promise.then(
      (val) => (hasCanceled_ ? reject({ isCanceled: true }) : resolve(val)),
      (error) => (hasCanceled_ ? reject({ isCanceled: true }) : reject(error)),
    );
  }).catch((e) => console.log(e));

  return {
    promise: wrappedPromise,
    cancel() {
      hasCanceled_ = true;
    },
  };
};

function fetchAllCompanies() {
  let request: RestRequestPayload = {
    method: "GET",
    relativeUrl: "loadAllCompanies",
    customHeaders: [ACCEPT_JSON_HEADER],
  };
  const baseUrl = (
    ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
  ).getBaseUrl();
  if (baseUrl.slice(-1) !== "/") {
    request = {
      ...request,
      relativeUrl: `/${request.relativeUrl}`,
    };
  }
  const response = (
    ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
  ).fetchData(request);
  return response.then((resp) => resp.json());
}

export function UpdateCompany(company: CompanyType) {
  let request: RestRequestPayload = {
    method: "POST",
    relativeUrl: "updateCompany",
    body: JSON.stringify(company),
    customHeaders: [ACCEPT_JSON_HEADER, CONTENT_TYPE_JSON_HEADER],
  };
  const baseUrl = (
    ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
  ).getBaseUrl();
  if (baseUrl.slice(-1) !== "/") {
    request = {
      ...request,
      relativeUrl: `/${request.relativeUrl}`,
    };
  }
  const response = (
    ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
  ).fetchData(request);
  return response.then((resp) => resp.text());
}

const warningTooltip = (
  <WarningTooltip key="warning" text="Be carefully with your changes" />
);
const hintTooltip = <HintTooltip key="hint" text="Can't change" />;

const ROWS_PER_PAGE = 10;
const ADDRESS_LIST: string[][] = [
  ["US", "America"],
  ["VN", "Vietnam"],
  ["CZ", "Czech"],
  ["FR", "France"],
  ["DE", "Germany"],
  ["IT", "Italy"],
];

type RowType = {
  id: string;
  name: string;
  countryCode: string;
  taxNumber: string;
};
const columns: BaseColumnType<RowType>[] = [
  { label: "ID", dataKey: "id", pinning: "left" },
  { label: "Name", dataKey: "name", pinning: "left" },
  { label: "Country code", dataKey: "countryCode" },
  { label: "Tax number", dataKey: "taxNumber" },
  {
    label: "",
    actionColumn: true,
    dataGetter: () => <Button key="edit-icon" icon={<Icon>edit</Icon>} />,
  },
];

export const EditableRowTable: React.ComponentType = () => {
  const [data, setData] = React.useState<RowType[]>([]);
  const [isShowErrorMessage, setShowErrorMessage] = React.useState(false);
  const [editedRowId, setEditedRowId] = React.useState<string | undefined>(
    undefined,
  );
  const [selectedRowId, setSelectedRowId] = React.useState<string | undefined>(
    undefined,
  );

  const selectedRowRef = React.useRef<HTMLElement | null>(null);
  const bodyRowRenderer = React.useCallback(
    (props: TableRenderPropsType.BodyRowProps<RowType>) => {
      const { row } = props;
      const wrapperRef =
        row.id === selectedRowId
          ? (ref: HTMLElement | null) => {
              if (ref) {
                selectedRowRef.current = ref;
              }
            }
          : undefined;
      if (editedRowId === row.id) {
        return (
          <CustomBodyRow
            key={row.id}
            {...props}
            setEditedRowId={setEditedRowId}
            editedRowId={editedRowId}
            setData={setData}
            wrapperRef={wrapperRef}
            setShowErrorMessage={setShowErrorMessage}
          />
        );
      } else {
        return DefaultTableComponentRenderers.bodyRowRenderer({
          ...props,
          interactive: true,
          wrapperRef,
          onClick: () => {
            if (row.id !== editedRowId) {
              setEditedRowId(row.id);
              setSelectedRowId(row.id);
            }
          },
        });
      }
    },
    [editedRowId, selectedRowId],
  );

  React.useEffect(() => {
    const fetchPromise = makeCancelable(
      fetchAllCompanies().then((resp) => {
        setData(resp);
      }),
    );
    return () => {
      fetchPromise.cancel();
    };
  }, []);

  React.useEffect(() => {
    selectedRowRef.current?.focus();
  }, [editedRowId]);

  const pageCount = React.useMemo(
    () => Math.floor((data.length - 1) / ROWS_PER_PAGE + 1),
    [data.length],
  );
  const [pageNumber, setPageNumber] = React.useState<number>(1);
  const pagedData = React.useMemo(() => {
    return data.slice(
      (pageNumber - 1) * ROWS_PER_PAGE,
      pageNumber * ROWS_PER_PAGE,
    );
  }, [data, pageNumber]);

  const renderError = () => {
    return isShowErrorMessage ? (
      <MessageBox
        label="You don't have authorization"
        variant="error"
        focusOnMessage={false}
        action={
          <Button label="Close" onClick={() => setShowErrorMessage(false)} />
        }
      />
    ) : (
      ""
    );
  };

  const handleOnPageChange = React.useCallback((pageNumber: number) => {
    setPageNumber(pageNumber);
    setEditedRowId(undefined);
    setSelectedRowId(undefined);
  }, []);

  return (
    <>
      {renderError()}
      <Table<RowType>
        columns={columns}
        data={pagedData}
        rowStyling={({ row }) => ({
          selected: row.id === selectedRowId,
          title: "Edit row",
        })}
        rowKey={({ row }) => row.id}
        componentRenderers={{
          bodyRowRenderer,
        }}
      />
      <Pagination
        id="extended-table-pagination"
        disabled={false}
        alignment="right"
        currentPage={pageNumber}
        pageCount={pageCount}
        onPageChanged={handleOnPageChange}
        pageLabelTemplate="{page} / {total}"
        key="paging"
      />
    </>
  );
};

const CustomBodyRow: React.ComponentType<
  TableRenderPropsType.BodyRowProps<RowType> & {
    editedRowId: string | undefined;
    setEditedRowId: Dispatch<SetStateAction<string | undefined>>;
    setData: Dispatch<SetStateAction<RowType[]>>;
    setShowErrorMessage: Dispatch<SetStateAction<boolean>>;
  }
> = (props) => {
  const context = useTableContext<RowType>((context) => context);
  const { row, setData, setEditedRowId, setShowErrorMessage } = props;
  const [clonedRow, setClonedRow] = React.useState<RowType | undefined>(
    undefined,
  );
  React.useEffect(() => {
    setClonedRow(row);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const bodyContentRenderer = React.useMemo(() => {
    return (props: any) => (
      <CustomBodyContent
        {...props}
        setClonedRow={setClonedRow}
        clonedRow={clonedRow}
        setData={setData}
        onEditDone={() => {
          setEditedRowId(undefined);
        }}
        onError={(error) => {
          if (error && error?.status === 403) {
            setShowErrorMessage(true);
          } else {
            setShowErrorMessage(false);
          }
        }}
      />
    );
  }, [clonedRow, setData, setEditedRowId, setShowErrorMessage]);

  return (
    <TableContextProvider
      value={{
        ...context,
        componentRenderers: {
          ...context.componentRenderers,
          bodyContentRenderer,
        },
      }}
    >
      {DefaultTableComponentRenderers.bodyRowRenderer(props)}
    </TableContextProvider>
  );
};

const CustomBodyContent: React.ComponentType<
  TableRenderPropsType.BodyContentProps<RowType> & {
    clonedRow: RowType | undefined;
    setClonedRow: Dispatch<SetStateAction<RowType | undefined>>;
    setData: Dispatch<SetStateAction<RowType[]>>;
    onEditDone: () => void;
    onError: (error: any) => void;
  }
> = (props) => {
  const { column, rowIndex, clonedRow, setClonedRow, setData } = props;
  const handleChange = React.useCallback((value: string) => {
    setClonedRow(
      (row) => ({ ...row, [`${column.dataKey}`]: value.trim() }) as RowType,
    );
  }, [column.dataKey, setClonedRow]);

  const key = `${column.label}, row ${rowIndex + 1}`;
  if (column.actionColumn) {
    return (
      <ButtonGroup>
        <Button
          primary
          destructive
          icon={<Icon>close</Icon>}
          title="Close"
          onKeyDown={handleActionButtonKeyDown}
          onClick={() => {
            props.onEditDone();
          }}
        />
        <Button
          primary
          icon={<Icon>check</Icon>}
          title="Save"
          onKeyDown={handleActionButtonKeyDown}
          onClick={() => {
            setData((data) =>
              data.map((row) => (row.id === clonedRow?.id ? clonedRow : row)),
            );
            UpdateCompany(clonedRow as CompanyType)
              .then((resp) => {
                console.info(resp);
                props.onEditDone();
              })
              .catch((error) => {
                console.error(error);
                props.onError(error);
              });
          }}
        />
      </ButtonGroup>
    );
  }
  const cellValue =
    column.dataKey !== undefined
      ? (getDataByKey(clonedRow, column.dataKey) as string)
      : "";
  const isDisabled = column.label === "Website";
  switch (column.label) {
    case "Business":
      return (
        <TextAreaStateless
          id={generateUid()}
          key={key}
          autoExpand
          value={cellValue}
          onChange={(event) => handleChange(event.target.value)}
          hideLabel
          addonAfter={warningTooltip}
        />
      );
    case "CountryCode":
      return (
        <Select
          id={generateUid()}
          value={cellValue}
          label={`${column.label}, row ${rowIndex + 1}`}
          hideLabel
          items={ADDRESS_LIST.map((item: string[]) => ({
            label: item[0],
            value: item[1],
          }))}
          onValueChanged={(value) => {
            handleChange(value);
          }}
        />
      );
    default:
      return (
        <TextLineStateless
          id={generateUid()}
          value={cellValue}
          disabled={isDisabled}
          label={`${column.label}, row ${rowIndex + 1}`}
          hideLabel
          onChange={(event) => handleChange(event.target.value)}
          addonAfter={isDisabled && hintTooltip}
        />
      );
  }
};

function handleActionButtonKeyDown(event: React.KeyboardEvent<HTMLElement>) {
  if (event.keyCode === Key.Enter) {
    event.stopPropagation();
  }
}
